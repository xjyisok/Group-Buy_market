package cn.sweater.types.design.framework.tree;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractStrategyMultiThreadRouter<T,D,R>implements StrategyHandler<T,D,R> ,StrategyMapper<T,D,R>{
    protected StrategyHandler<T,D,R> defaultstrategyHandler=StrategyHandler.DEFAULT;
    public R router(T requestParameter,D dynamicContext)throws Exception{
        StrategyHandler<T,D,R>strategyHandler=get(requestParameter,dynamicContext);
        if(strategyHandler!=null){
            return strategyHandler.apply(requestParameter,dynamicContext);
        }
        return defaultstrategyHandler.apply(requestParameter,dynamicContext);
    }

    @Override
    public R apply(T requestParameter, D dynamicContext) throws Exception {
        multithread(requestParameter,dynamicContext);
        return doapply(requestParameter,dynamicContext);
    }

    protected abstract R doapply(T requestParameter, D dynamicContext)throws Exception;

    protected abstract void multithread(T requestParameter, D dynamicContext) throws ExecutionException, InterruptedException, TimeoutException;
}
