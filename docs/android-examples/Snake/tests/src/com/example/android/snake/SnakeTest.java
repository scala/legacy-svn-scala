package com.example.android.snake;

import android.test.ActivityInstrumentationTestCase;

import com.example.android.snake.Snake;

/**
 * Make sure that the main launcher activity opens up properly, which will be
 * verified by {@link ActivityTestCase#testActivityTestCaseSetUpProperly}.
 */
public class SnakeTest extends ActivityInstrumentationTestCase<Snake> {
  
  public SnakeTest() {
      super("com.example.android.snake", Snake.class);
  }
  
}
