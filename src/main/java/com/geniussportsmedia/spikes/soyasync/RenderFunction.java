package com.geniussportsmedia.spikes.soyasync;

import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.SoyValueConverter;
import com.google.template.soy.data.SoyValueProvider;
import com.google.template.soy.shared.restricted.SoyFunction;
import com.google.template.soy.shared.restricted.SoyJavaFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.Collections.singleton;

public class RenderFunction implements SoyJavaFunction, SoyFunction {

    private static final Logger logger = LoggerFactory.getLogger(RenderFunction.class);

    private final SoyValueConverter valueConverter;
    private final Executor executor;

    @Inject
    public RenderFunction(final SoyValueConverter valueConverter, Executor executor) {
        this.valueConverter = valueConverter;
        this.executor = executor;
    }

    public SoyValue computeForJava(final List<SoyValue> list) {
        final CompletableFuture<String> future = getFuture(list.get(0).coerceToString());
        final SoyValueProvider provider = valueConverter.convert(future);
        return new SoyProviderFacadeValue(provider);
    }

    private CompletableFuture<String> getFuture(final String soyValue) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("starting future async of: " + soyValue);
            try {
                Thread.sleep(5000);
                logger.info("done calculating: " + soyValue);
                return "VALUE[" + soyValue + "]";
            } catch (InterruptedException e) {
                logger.error("error", e);
                return "ERROR[" + soyValue + "]";
            }
        }, executor);
    }

    public String getName() {
        return "render";
    }

    public Set<Integer> getValidArgsSizes() {
        return singleton(1);
    }
}
