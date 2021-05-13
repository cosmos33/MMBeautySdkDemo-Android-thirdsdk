package io.agora.api.example;

import android.app.Application;
import android.content.Context;

import java.lang.annotation.Annotation;
import java.util.Set;

import io.agora.api.example.annotation.Example;
import io.agora.api.example.common.model.Examples;
import io.agora.api.example.utils.ClassUtils;

public class AgoraApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initExamples(this);
    }

    static private void initExamples(Context context) {
        try {
            Set<String> packageName = ClassUtils.getFileNameByPackageName(context, "io.agora.api.example.examples");
            for (String name : packageName) {
                Class<?> aClass = Class.forName(name);
                Annotation[] declaredAnnotations = aClass.getAnnotations();
                for (Annotation annotation : declaredAnnotations) {
                    if (annotation instanceof Example) {
                        Example example = (Example) annotation;
                        Examples.addItem(example);
                    }
                }
            }
            Examples.sortItem();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
