package pkg.asyncexcute;
public interface AsyncExecutorAware<T> {
	public void setAsyncExecutor(AsyncExecutor<T> asyncExecutor);
}
