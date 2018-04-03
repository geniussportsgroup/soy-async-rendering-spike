package com.geniussportsmedia.spikes.soyasync;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.SoyModule;
import com.google.template.soy.shared.restricted.SoyFunction;
import com.google.template.soy.tofu.SoyTofu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

public class Renderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Renderer.class);

    public static void main(String[] args) {
        final Injector injector = createInjector();
        final SoyTofu tofu = injector.getInstance(SoyTofu.class);
        printTemplate(tofu, "foo.MyTemplate");
        printTemplate(tofu, "foo.MyTemplate2");
    }

    private static void printTemplate(final SoyTofu tofu, final String templateName) {
        System.err.println("------------------------------------");
        System.err.println("printing template - " + templateName);

        final Instant start = Instant.now();
        tofu.newRenderer(templateName).render();
        final Instant stop = Instant.now();

        System.err.println("took: " + Duration.between(start, stop).toMillis() + "ms");
        System.err.println("------------------------------------");
        System.err.println("");
        System.err.flush();
    }

    public static Injector createInjector() {
        return Guice.createInjector(new MyModule());
    }

    private static class MyModule extends AbstractModule {

        @Override
        protected void configure() {
            install(new SoyModule());
            newSetBinder(binder(), SoyFunction.class).addBinding().to(RenderFunction.class);
        }

        @Provides
        public Executor executor() {
            return Executors.newFixedThreadPool(5);
        }

        @Provides
        public SoyTofu tofu(SoyFileSet.Builder builder) {
            builder.add(Renderer.class.getResource("/template.soy"));
            return builder.build().compileToTofu();
        }
    }
}
