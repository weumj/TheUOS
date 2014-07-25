package pkg.asyncexcute;
public interface AsyncCallback<T> {
    public void onResult(T result);
    public void exceptionOccured(Exception e);
    public void cancelled();
    public void onPostExcute();
    
    public static abstract class Base<T> implements AsyncCallback<T> {
		@Override
		public void exceptionOccured(Exception e) {
		}
		@Override
		public void cancelled() {
		}
		@Override
		public void onPostExcute() {
		}
	}
}