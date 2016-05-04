package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.QuestionType;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class QuestionTypeTableCell implements Callback<TableColumn.CellDataFeatures<Answer, QuestionType>, ObservableValue<QuestionType>>
{
    private QuestionType type;

    public QuestionTypeTableCell(QuestionType type)
    {
        this.type = type;
    }

    @Override
    public ObservableValue<QuestionType> call(TableColumn.CellDataFeatures<Answer, QuestionType> param)
    {
        return new ReadOnlyObjectWrapper<QuestionType>(this.type);
    }
}
