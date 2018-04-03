package com.geniussportsmedia.spikes.soyasync;

import com.google.template.soy.data.LoggingAdvisingAppendable;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.SoyValueProvider;
import com.google.template.soy.jbcsrc.api.RenderResult;

import javax.annotation.Nonnull;
import java.io.IOException;

public class SoyProviderFacadeValue implements SoyValue {

    private final SoyValueProvider soyValueProvider;

    public SoyProviderFacadeValue(final SoyValueProvider soyValueProvider) {
        this.soyValueProvider = soyValueProvider;
    }

    @Nonnull
    @Override
    public SoyValue resolve() {
        return soyValueProvider.resolve();
    }

    @Nonnull
    @Override
    public RenderResult status() {
        return soyValueProvider.status();
    }

    @Override
    public RenderResult renderAndResolve(final LoggingAdvisingAppendable appendable,
                                         final boolean b) throws IOException {
        return soyValueProvider.renderAndResolve(appendable, b);
    }

    @Override
    public boolean coerceToBoolean() {
        return resolve().coerceToBoolean();
    }

    @Override
    public String coerceToString() {
        return resolve().coerceToString();
    }

    @Override
    public void render(final LoggingAdvisingAppendable appendable) throws IOException {
        resolve().render(appendable);
    }

    @Override
    public void render(final Appendable appendable) throws IOException {
        resolve().render(appendable);
    }

    @Override
    public boolean booleanValue() {
        return resolve().booleanValue();
    }

    @Override
    public int integerValue() {
        return resolve().integerValue();
    }

    @Override
    public long longValue() {
        return resolve().longValue();
    }

    @Override
    public double floatValue() {
        return resolve().floatValue();
    }

    @Override
    public double numberValue() {
        return resolve().numberValue();
    }

    @Override
    public String stringValue() {
        return resolve().stringValue();
    }
}
