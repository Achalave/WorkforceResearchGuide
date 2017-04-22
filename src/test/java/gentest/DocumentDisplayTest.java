package gentest;

import javax.swing.JFrame;
import utd.team6.workforceresearchguide.gui.DocumentDisplay;
import utd.team6.workforceresearchguide.main.DocumentData;



//@author Michael Haertling

public class DocumentDisplayTest {
    
    public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.add(new DocumentDisplay(new DocumentData("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxtest.pdf")));
        frame.pack();
        frame.setVisible(true);
    }
    
}
