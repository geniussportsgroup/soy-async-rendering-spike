## spike report:

Given a `render(url)` function that is expensive, and the following template:

```soy
{template .MyTemplate}
    {let $a: render('/feed/A.json') /}
    {let $b: render('/feed/B.json') /}
    {let $c: render('/feed/C.json') /}
    {$b}{$a}{$c}
{/template}

```

Is it possible to have invocations of `render()` execute in parallel?  It would seem that the type 
`SoyValueProvider` would allow for this, in that the `resolve()` method could be lazily evaluated.

I've created [RenderFunction.java](/src/main/java/com/geniussportsmedia/spikes/soyasync/RenderFunction.java) file 
to emulate an expensive operation:2

```java
public SoyValue computeForJava(final List<SoyValue> list) {
    final CompletableFuture<String> future = getFuture(list.get(0).coerceToString());
    final SoyValueProvider provider = valueConverter.convert(future);
    return new SoyProviderFacadeValue(provider);
}

private CompletableFuture<String> getFuture(final String url) {
    return CompletableFuture.supplyAsync(() -> {
        logger.info("starting future async of: " + url);
        try {
            Thread.sleep(5000);
            logger.info("done calculating: " + url);
            return "VALUE[" + url + "]";
        } catch (InterruptedException e) {
            logger.error("error", e);
            return "ERROR[" + url + "]";
        }
    }, executor);
}
```

*see: [SoyProviderFacadeValue](/src/main/java/com/geniussportsmedia/spikes/soyasync/SoyProviderFacadeValue.java)*

Running the template above yields the following output:

```bash

------------------------------------
printing template - foo.MyTemplate
[pool-1-thread-1] INFO com.geniussportsmedia.spikes.soyasync.RenderFunction - starting future async of: /feed/B.json
[pool-1-thread-1] INFO com.geniussportsmedia.spikes.soyasync.RenderFunction - done calculating: /feed/B.json
[pool-1-thread-2] INFO com.geniussportsmedia.spikes.soyasync.RenderFunction - starting future async of: /feed/A.json
[pool-1-thread-2] INFO com.geniussportsmedia.spikes.soyasync.RenderFunction - done calculating: /feed/A.json
[pool-1-thread-3] INFO com.geniussportsmedia.spikes.soyasync.RenderFunction - starting future async of: /feed/C.json
[pool-1-thread-3] INFO com.geniussportsmedia.spikes.soyasync.RenderFunction - done calculating: /feed/C.json
took: 15074ms
------------------------------------

```

All the operations are done synchronously. Ideally all the `render(url)` calls would happen in parallel as 
the reduce the total wait time.

A workaround we found to achieve this was to use to store the operation results in a map literal:


```soy

{template .MyTemplate2}
    {let $data: [
      'd': render('/feed/C.json'),
      'e': render('/feed/E.json'),
      'f': render('/feed/F.json')
    ] /}
    {$data.d}
    {$data.e}
    {$data.f}
{/template}

```


```bash

------------------------------------
printing template - foo.MyTemplate2
[pool-1-thread-4] INFO com.geniussportsmedia.spikes.soyasync.RenderFunction - starting future async of: /feed/C.json
[pool-1-thread-5] INFO com.geniussportsmedia.spikes.soyasync.RenderFunction - starting future async of: /feed/E.json
[pool-1-thread-1] INFO com.geniussportsmedia.spikes.soyasync.RenderFunction - starting future async of: /feed/F.json
[pool-1-thread-4] INFO com.geniussportsmedia.spikes.soyasync.RenderFunction - done calculating: /feed/C.json
[pool-1-thread-5] INFO com.geniussportsmedia.spikes.soyasync.RenderFunction - done calculating: /feed/E.json
[pool-1-thread-1] INFO com.geniussportsmedia.spikes.soyasync.RenderFunction - done calculating: /feed/F.json
took: 5003ms
------------------------------------

```
