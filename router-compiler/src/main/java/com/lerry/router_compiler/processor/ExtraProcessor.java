package com.lerry.router_compiler.processor;

import com.google.auto.service.AutoService;
import com.lerry.router_annotation.Extra;
import com.lerry.router_compiler.utils.Constants;
import com.lerry.router_compiler.utils.Utils;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Gmail: lerryletter@gmail.com
 * -----------
 * Blog: imlerry.com
 *
 * @author lerry on 2018/4/26.
 *         处理属性上的注解
 */
@AutoService(Processor.class)
@SupportedOptions(Constants.ARGUMENTS_NAME)
@SupportedAnnotationTypes(Constants.ANN_TYPE_EXTRA)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ExtraProcessor extends AbstractProcessor {
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //是否使用了注解
        if (!Utils.isEmpty(set)) {
            Set<? extends Element> extraElementSets = roundEnvironment.getElementsAnnotatedWith(Extra.class);

        }
        return false;
    }
}
