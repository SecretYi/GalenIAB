package com.picfun.apt;

import com.google.auto.service.AutoService;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(value = Processor.class)
public class Proxy extends AbstractProcessor {

    private Elements mElements;
    private Messager mMessager;
    private Filer mFiler;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportAnnotationSets = new LinkedHashSet<>();
        supportAnnotationSets.add(StringValue.class.getCanonicalName());
        return supportAnnotationSets;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElements = processingEnvironment.getElementUtils();
        mMessager = processingEnvironment.getMessager();
        mFiler = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        msg("process start...");

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(StringValue.class);
        for (Element e : elements) {
            if(e.getKind() == ElementKind.FIELD){
                VariableElement variableElement = (VariableElement) e;

                msg(variableElement.getSimpleName() + "==value=="+variableElement.getConstantValue());
            }
        }

        return false;
    }

    private void msg(Object o) {
        if (null != mMessager) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, o.toString());
        }
    }

}
