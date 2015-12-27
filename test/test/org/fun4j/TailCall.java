package test.org.fun4j;

/**
 * Created by thomas on 13.07.2014.
 */
public abstract class TailCall<T> {

  public static <T> TailCall<T> call(final TailCall<T> nextCall) {
    return nextCall;
  }

  public static <T> TailCall<T> done(final T value) {
    return new TailCall<T>() {
      @Override
      public boolean isComplete() { return true; }
      @Override
      public T result() { return value; }
      @Override
      public TailCall<T> apply() {
        throw new Error("finished! no more calls!");
      }
    };

  }

  public abstract TailCall<T> apply();

  public boolean isComplete(){
    return false;
  }

  public T result() {
    throw new Error("not implemented");
  }

  public T invoke() {
    TailCall<T> candidate = this;
    while (! candidate.isComplete()) {
      candidate = candidate.apply();
    }
    return candidate.result();
  }

}
