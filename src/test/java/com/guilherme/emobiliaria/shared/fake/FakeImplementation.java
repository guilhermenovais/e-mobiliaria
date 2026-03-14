package com.guilherme.emobiliaria.shared.fake;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public abstract class FakeImplementation {
  private final AtomicReference<Supplier<? extends RuntimeException>> nextFailure =
      new AtomicReference<>();

  public void failNext(Supplier<? extends RuntimeException> failure) {
    nextFailure.set(failure);
  }

  void maybeFail() {
    Supplier<? extends RuntimeException> f = nextFailure.getAndSet(null);
    if (f != null) {
      nextFailure.set(null);
      throw f.get();
    }
  }
}
