/**
 * SonarLint for IntelliJ IDEA
 * Copyright (C) 2015 SonarSource
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonarlint.intellij.analysis;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@ThreadSafe
public class SonarLintStatus extends AbstractProjectComponent {
  private Status status = Status.STOPPED;
  //lock free list
  private Queue<Listener> listeners = new ConcurrentLinkedQueue<>();

  public SonarLintStatus(Project project) {
    super(project);
  }

  public enum Status {RUNNING, STOPPED, CANCELLING}

  public interface Listener {
    void callback(Status newStatus);
  }

  public static SonarLintStatus get(Project p) {
    return p.getComponent(SonarLintStatus.class);
  }

  @Override
  public void disposeComponent() {
    synchronized (this) {
      listeners.clear();
    }
  }

  public synchronized boolean isRunning() {
    return status == Status.RUNNING || status == Status.CANCELLING;
  }

  public synchronized boolean isCanceled() {
    return status == Status.CANCELLING;
  }

  public void stopRun() {
    Status callback = null;
    synchronized (this) {
      if (isRunning()) {
        status = Status.STOPPED;
        callback = status;
      }
    }

    //don't lock while calling listeners
    if (callback != null) {
      callListeners(callback);
    }
  }

  public void cancel() {
    Status callback = null;
    synchronized (this) {
      if (status == Status.RUNNING) {
        status = Status.CANCELLING;
        callback = Status.CANCELLING;
      }
    }

    //don't lock while calling listeners
    if (callback != null) {
      callListeners(callback);
    }
  }

  public boolean tryRun() {
    Status callback = null;
    synchronized (this) {
      if (!isRunning()) {
        status = Status.RUNNING;
        callback = Status.RUNNING;
      }
    }

    //don't lock while calling listeners
    if (callback != null) {
      callListeners(callback);
      return true;
    } else {
      return false;
    }
  }

  public void subscribe(Listener listener) {
    listeners.add(listener);
  }

  private void callListeners(Status status) {
    for (Listener l : listeners) {
      l.callback(status);
    }
  }
}
