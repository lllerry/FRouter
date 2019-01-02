package com.lerry.router_compiler.processor;

import com.google.auto.service.AutoService;
import com.lerry.router_annotation.Route;
import com.lerry.router_annotation.model.RouteMeta;
import com.lerry.router_compiler.utils.Constants;
import com.lerry.router_compiler.utils.Log;
import com.lerry.router_compiler.utils.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;


@AutoService(Processor.class)
/**
 * 处理器接受的参数
 */
@SupportedOptions(Constants.ARGUMENTS_NAME)
/**
 * 指定jdk使用版本
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
/**
 * 注明是给哪些注解使用的
 */
@SupportedAnnotationTypes(Constants.ANN_TYPE_ROUTE)
public class RouteProcessor extends AbstractProcessor {

    private Log mLog;
    /**
     * 文件生成器 类资源
     */
    private Filer mFilerUtils;
    /**
     * 类信息工具类
     */
    private Types mTypeUtils;
    /**
     * 节点工具类(类,函数,属性都是节点)
     */
    private Elements mElementUtils;
    /**
     * 模块名称
     */
    private String mModuleName;
    /**
     * 分组信息 key:组名,value 组内路由信息
     */
    private Map<String, List<RouteMeta>> groupMap = new HashMap<>();
    /**
     * key:组名 value:类名
     */
    private Map<String, String> rootMap = new TreeMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //apt的日志输出
        mLog = Log.newLog(processingEnvironment.getMessager());
        mElementUtils = processingEnv.getElementUtils();
        mTypeUtils = processingEnv.getTypeUtils();
        mFilerUtils = processingEnv.getFiler();
        Map<String, String> options = processingEnv.getOptions();
        if (!Utils.isEmpty(options)) {
            mModuleName = options.get(Constants.ARGUMENTS_NAME);
        }
    }

    /**
     * 处理注解的主要函数
     *
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //使用了需要处理的注解
        if (!Utils.isEmpty(set)) {
            //获取所有被route注解的元素集合
            Set<? extends Element> routeElements = roundEnvironment.getElementsAnnotatedWith(Route.class);
            //处理route注解
            if (!Utils.isEmpty(routeElements)) {
                try {
                    parseRouteElements(routeElements);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return false;
    }

    private void parseRouteElements(Set<? extends Element> routeElements) throws IOException {
        //支持配置路由的类型
        TypeElement activityTypeElement = mElementUtils.getTypeElement(Constants.ACTIVITY);
        //节点自描述
        TypeMirror typeMirrorActivity = activityTypeElement.asType();

        TypeElement serviceTypeElement = mElementUtils.getTypeElement(Constants.ISERVICE);
        TypeMirror typeMirrorIservice = serviceTypeElement.asType();

        //声明route注解的所有节点
        for (Element routeElement : routeElements) {
            //保存路由信息的javabean
            RouteMeta routeMeta;
            //使用route注解的类信息
            TypeMirror typeMirrorRoute = routeElement.asType();
            mLog.i("Route class = " + typeMirrorRoute.toString());

            Route route = routeElement.getAnnotation(Route.class);
            //判断是activity使用了注解
            if (mTypeUtils.isSubtype(typeMirrorRoute, typeMirrorActivity)) {
                routeMeta = new RouteMeta(RouteMeta.Type.ACTIVITY, route, routeElement);
            } else if (mTypeUtils.isSubtype(typeMirrorRoute, typeMirrorIservice)) {
                routeMeta = new RouteMeta(RouteMeta.Type.ISERVICE, route, routeElement);
            } else {
                throw new RuntimeException("[Just Support Activity/IService Route] :" + routeElement);
            }

            //记录分组信息
            categories(routeMeta);
        }

        //创建生成需要继承的接口
        TypeElement typeElementIRouteGroup = mElementUtils.getTypeElement(Constants.IROUTE_GROUP);
        TypeElement typeElementIRouteRoot = mElementUtils.getTypeElement(Constants.IROUTE_ROOT);

        //生成组类
        generateGroup(typeElementIRouteGroup);
        //生成root类,作用 记录<分组,对应的group类>
        generateRoot(typeElementIRouteRoot, typeElementIRouteGroup);

    }

    private void generateRoot(TypeElement elementIRouteRoot, TypeElement typeElementIRouteGroup) throws IOException {
        //生成参数类型 Map<String, Class<? extends IRouteGroup>>
        ParameterizedTypeName routes = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(typeElementIRouteGroup)))
        );

        //参数 Map<String,Class<? extends IRouteGroup>> routes> routes
        ParameterSpec routeParameterSpec = ParameterSpec.builder(routes, "routes").build();

//        声明函数
        MethodSpec.Builder loadIntoMehodOfRootBuilder = MethodSpec.methodBuilder(Constants.METHOD_LOAD_INTO)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(routeParameterSpec);

        //声明方法体
        for (Map.Entry<String, String> entity : rootMap.entrySet()) {
            String statement = "routes.put($S,$T.class)";
            loadIntoMehodOfRootBuilder.addStatement(statement, entity.getKey(), ClassName.get(Constants.PACKAGE_OF_GENERATE_FILE, entity.getValue()));
        }

        //生成root文件
        String rootClassName = Constants.NAME_OF_ROOT + mModuleName;
        JavaFile.builder(Constants.PACKAGE_OF_GENERATE_FILE,
                TypeSpec.classBuilder(rootClassName)
                        .addMethod(loadIntoMehodOfRootBuilder.build())
                        .addModifiers(Modifier.PUBLIC)
                        .addSuperinterface(ClassName.get(elementIRouteRoot))
                        .build()
        ).build().writeTo(mFilerUtils);

        mLog.i("Generated RouteRoot: " + Constants.PACKAGE_OF_GENERATE_FILE + "." + rootClassName);
    }

    /**
     * 生成 组信息类
     *
     * @param typeElementIRouteGroup
     */
    private void generateGroup(TypeElement typeElementIRouteGroup) throws IOException {
        //生成参数  Map<String,RouteMeta>
        ParameterizedTypeName altas = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), ClassName.get(RouteMeta.class));
        //参数 Map<String,RouteMeta> atlas
        ParameterSpec groupParams = ParameterSpec.builder(altas, "altas").build();
        //遍历分组 为每个分组创建一个$$group$$类
        for (Map.Entry<String, List<RouteMeta>> entry : groupMap.entrySet()) {
            /**
             * 类成员函数的声明和构建
             */
            //函数 public void loadInfo(Map<String,RouteMeta> atlas)
            MethodSpec.Builder loadIntoMethodOfGroupBuilder = MethodSpec.methodBuilder(Constants.METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(groupParams);

            //组名和对应分组中的信息
            String groupName = entry.getKey();
            List<RouteMeta> groupInfo = entry.getValue();
            //遍历分组中条目的数据
            for (RouteMeta routeMeta : groupInfo) {
                String statement = "altas.put($S,$T.build($T.$L,$T.class,$S,$S))";
                loadIntoMethodOfGroupBuilder.addStatement(
                        statement,
                        routeMeta.getPath(),
                        ClassName.get(RouteMeta.class),
                        ClassName.get(RouteMeta.Type.class),
                        routeMeta.getType(),
                        ClassName.get((TypeElement) routeMeta.getElement()),
                        routeMeta.getPath().toLowerCase(),
                        routeMeta.getGroup().toLowerCase()
                );
            }

            //创建文件
            String groupClassName = Constants.NAME_OF_GROUP + groupName;
            JavaFile.builder(Constants.PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(groupClassName)
                            .addSuperinterface(ClassName.get(typeElementIRouteGroup))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(loadIntoMethodOfGroupBuilder.build())
                            .build()
            ).build().writeTo(mFilerUtils);

            mLog.i("Generated RouteGroup: " + Constants.PACKAGE_OF_GENERATE_FILE + "." +
                    groupClassName);

            //分组名和对应生成的group类的类名
            rootMap.put(groupName, groupClassName);
        }
    }


    private void categories(RouteMeta routeMeta) {
        //验证route
        if (routeVerify(routeMeta)) {
            mLog.i("Group Info, Group Name = " + routeMeta.getGroup() + ", Path = " +
                    routeMeta.getPath());

            List<RouteMeta> routeMetas = groupMap.get(routeMeta.getGroup());
//            如果原先组里没有 再创建 否则就添加进组中
            if (Utils.isEmpty(routeMetas)) {
                List<RouteMeta> routeMetaSet = new ArrayList<>();
                routeMetaSet.add(routeMeta);
                groupMap.put(routeMeta.getGroup(), routeMetaSet);
            } else {
                routeMetas.add(routeMeta);
            }
        } else {
            mLog.i("Group Info Error: " + routeMeta.getPath());
        }
    }

    private boolean routeVerify(RouteMeta routeMeta) {
        boolean isCorrect = false;
        String path = routeMeta.getPath();
        String group = routeMeta.getGroup();
        //路由地址必须以"/"开头
        if (Utils.isEmpty(path) || !path.startsWith(Constants.SLASH)) {
            isCorrect = false;
        }

        //如果没有分组 以第一个/后的节点设为分组信息
        if (Utils.isEmpty(group)) {
            String defaultGroup = path.substring(1, path.indexOf(Constants.SLASH, 1));
            if (Utils.isEmpty(defaultGroup)) {
                isCorrect = false;
            } else {
                routeMeta.setGroup(defaultGroup);
                isCorrect = true;
            }

        }
        return isCorrect;
    }
}
