package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.QuestionType;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.io.IOException;

public class AnswerListCell extends ListCell<Answer>
{
    QuestionType type = QuestionType.SINGLE;

    public void setType(QuestionType type)
    {
        this.type = type;
    }

    @Override
    protected void updateItem(Answer item, boolean empty)
    {
        super.updateItem(item, empty);

        if(!empty && item != null)
        {
            GridPane rootGrid = null;
            Text textTxt, valueTxt;
            try
            {
                switch(type)
                {
                    case SINGLE:
                        rootGrid = (GridPane) FXMLLoader.load(AnswerListCell.class.getResource("/client/answer_single.fxml"));
                        break;
                    case MULTIPLE:
                        rootGrid = (GridPane) FXMLLoader.load(AnswerListCell.class.getResource("/client/answer_multiple.fxml"));
                        break;
                    default:
                        this.setText(null);
                        break;
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
            if(rootGrid != null)
            {
                textTxt = (Text) rootGrid.lookup("#textTxt");
                textTxt.setText(item.getText());
                valueTxt = (Text) rootGrid.lookup("#valueTxt");
                valueTxt.setText(String.valueOf(item.getValue()));
            }
            this.setGraphic(rootGrid);
        }
        else
        {
            this.setText(null);
            this.setGraphic(null);
        }
    }
}