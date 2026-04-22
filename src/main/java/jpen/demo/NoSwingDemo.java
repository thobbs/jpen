package jpen.demo;

import jpen.PLevel;
import jpen.PButtonEvent;
import jpen.PLevelEvent;
import jpen.PKindEvent;
import jpen.PScrollEvent;
import jpen.PenManager;
import jpen.event.PenAdapter;
import jpen.owner.HeadlessPenOwner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Headless demo: drives the full {@link PenManager} pipeline with a {@link HeadlessPenOwner} — no
 * AWT Components, no Swing window — and logs pressure values delivered by the pen provider. Runs
 * for 20 seconds.
 */
public class NoSwingDemo {

  private static final Logger L = LoggerFactory.getLogger(NoSwingDemo.class);

  public static void main(String[] args) {
    L.info("Starting NoSwingDemo...");
    new NoSwingDemo().start();
  }

  public void start() {
    PenManager penManager = new PenManager(new HeadlessPenOwner());
    penManager.pen.addListener(
        new PenAdapter() {

          @Override
          public void penKindEvent(PKindEvent ev) {
            L.info("Received penKindEvent: kindType={}, event={}", ev.kind.getType(), ev);
          }

          @Override
          public void penLevelEvent(PLevelEvent ev) {
            for (PLevel level : ev.levels) {
              if (level.getType() == PLevel.Type.PRESSURE) {
                L.info("Received penLevelEvent: levelType={}, value={}", level.getType(), level.value);
              } else {
                L.debug("Received penLevelEvent: levelType={}, value={}", level.getType(), level.value);
              }
            }
          }

          @Override
          public void penButtonEvent(PButtonEvent ev) {
            L.info("Received penButtonEvent: buttonType={}, event={}", ev.button.getType(), ev);
          }

          @Override
          public void penScrollEvent(PScrollEvent ev) {
            L.info("Received penScrollEvent: scrollType={}, event={}", ev.scroll.getType(), ev);
          }
        });

    long startTime = System.currentTimeMillis();
    // run for 12 seconds
    while (System.currentTimeMillis() - startTime < 12000) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
}
