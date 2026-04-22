package jpen.demo;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import jpen.PButtonEvent;
import jpen.PKindEvent;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.PScrollEvent;
import jpen.PenManager;
import jpen.event.PenAdapter;
import jpen.owner.multiAwt.AwtPenToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal Swing demo: opens a {@link JFrame} whose content pane is wired into the {@link
 * PenManager} via {@link AwtPenToolkit}. Logs pressure, kind, button, and scroll events delivered
 * while the pen is over that window.
 */
public class SimpleLoggingDemo {

  private static final Logger L = LoggerFactory.getLogger(SimpleLoggingDemo.class);

  public static void main(String[] args) {
    L.info("Starting SimpleLoggingDemo...");
    SwingUtilities.invokeLater(() -> new SimpleLoggingDemo().start());
  }

  public void start() {
    JFrame frame = new JFrame("SimpleLoggingDemo");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    JPanel canvas = new JPanel();
    canvas.setPreferredSize(new Dimension(640, 480));
    frame.setContentPane(canvas);

    PenManager penManager = AwtPenToolkit.getPenManager();
    penManager.pen.setFirePenTockOnSwing(true);
    penManager.pen.setFrequencyLater(40);

    AwtPenToolkit.addPenListener(canvas, new LoggingPenAdapter());

    frame.pack();
    frame.setLocationByPlatform(true);
    frame.setVisible(true);
    L.info("JFrame visible; move the pen over the window to see events.");
  }

  private static final class LoggingPenAdapter extends PenAdapter {

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
  }
}
