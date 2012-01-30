/*
 * All content copyright (c) Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.modules.hibernatecache.presentation;

import org.terracotta.modules.configuration.Presentation;
import org.terracotta.modules.configuration.PresentationContext;
import org.terracotta.modules.configuration.PresentationFactory;

public class HibernateCachePresentationFactory implements PresentationFactory {
  public Presentation create(PresentationContext context) {
    return new HibernatePresentationPanel();
  }
}