package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Client extends Application
{
    //- Main -
    public static void main(String[] args)
    {
        Client.launch(args);
    }

    //- Data -
    private List<Poll> polls = new ArrayList<>();
    private Poll poll;

    //- View -
    private ListView<Poll> pollList = new ListView<>();
    private TextField titleTxF;
    private TextArea descTxF;
    private TextField createdDateTxF, createdTimeTxF;
    private ComboBox<PollState> stateCbo;
    private ListView<Question> questionList;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        //Title
        primaryStage.setTitle("JWebPoll");

        //Default Poll: new poll
        Poll newPoll = new Poll("Neue Umfrage", "", PollState.NEW);
        Question newQuestion = new Question("Neue Frage", true, QuestionType.SINGLE);
        newQuestion.setHint("Hinweis zur Frage hier eingeben...");
        newPoll.getQuestions().add(newQuestion);
        polls.add(newPoll);

        //Example polls (to be deleted in future)
        polls.add(new Poll("1. Umfrage", "Eine Beschreibung", PollState.OPEN));
        polls.add(new Poll("Bundestagswahl", "Kurze Beschreibung", PollState.CLOESED));

        //ListView (Left side)
        SplitPane rootSplit = (SplitPane) FXMLLoader.load(this.getClass().getResource("/client/client.fxml"));
        this.pollList.setCellFactory((ListView<Poll> param) ->
        {
            return new PollListCell();
        });
        for (Poll p : this.polls)
        {
            this.pollList.getItems().add(p);
        }
        this.pollList.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Poll> observable, Poll oldValue, Poll newValue) ->
        {
            Client.this.setPoll(newValue);
        });
        rootSplit.getItems().add(this.pollList);
        rootSplit.setDividerPositions(1d / 4d);

        //PollView (Right side)
        ScrollPane pollDetailScroller = new ScrollPane();
        pollDetailScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pollDetailScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pollDetailScroller.setFitToWidth(true);
        pollDetailScroller.setFitToHeight(true);
        GridPane pollDetail = (GridPane) FXMLLoader.load(this.getClass().getResource("/client/pollView.fxml"));
        this.titleTxF = (TextField) pollDetail.lookup("#titleTxF");
        this.titleTxF.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
        {
            if(Client.this.poll != null
            && !Client.this.poll.getTitle().equals(newValue))
                Client.this.poll.setTitle(newValue);
        });
        this.descTxF = (TextArea) pollDetail.lookup("#descTxF");
        this.descTxF.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
        {
            if(Client.this.poll != null
            && !Client.this.poll.getDescription().equals(newValue))
                Client.this.poll.setDescription(newValue);
        });
        this.createdDateTxF = (TextField) pollDetail.lookup("#createdDateTxF");
        this.createdTimeTxF = (TextField) pollDetail.lookup("#createdTimeTxF");
        this.stateCbo = (ComboBox<PollState>) pollDetail.lookup("#stateCbo");
        this.stateCbo.getItems().addAll(PollState.NEW, PollState.OPEN, PollState.CLOESED);
        this.stateCbo.setCellFactory((ListView<PollState> param) ->
        {
            return new PollStateCell();
        });
        this.stateCbo.setButtonCell(new PollStateCell());
        this.questionList = (ListView<Question>) pollDetail.lookup("#questionList");
        this.questionList.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Question> observable, Question oldValue, Question newValue) ->
        {
            Platform.runLater(() -> Client.this.questionList.getSelectionModel().select(-1));
        });
        this.questionList.setCellFactory((ListView<Question> param) ->
        {
            return new QuestionListCell();
        });
        this.pollList.getSelectionModel().selectFirst();
        pollDetailScroller.setContent(pollDetail);
        rootSplit.getItems().add(pollDetailScroller);

        //Stage size and finally show
        primaryStage.setScene(new Scene(rootSplit, 800, 600));
        primaryStage.show();
    }

    public void setPoll(Poll newPoll)
    {
        this.poll = newPoll;
        if(this.poll != null)
        {
            this.titleTxF.setText(this.poll.getTitle());
            this.descTxF.setText(this.poll.getDescription());

            SimpleDateFormat outputFormatDate = new SimpleDateFormat("dd.MM.yyyy");
            SimpleDateFormat outputFormatTime = new SimpleDateFormat("HH:mm:ss");
            this.createdDateTxF.setText(outputFormatDate.format(this.poll.getCreated()));
            this.createdTimeTxF.setText(outputFormatTime.format(this.poll.getCreated()));
            this.stateCbo.setValue(this.poll.getState());
            this.questionList.getItems().clear();
            for (Question q : this.poll.getQuestions())
            {
                this.questionList.getItems().add(q);
            }
        }
    }

    //- Classes -
    private class PollListCell extends ListCell<Poll>
    {
        @Override
        protected void updateItem(Poll item, boolean empty)
        {
            super.updateItem(item, empty);

            if(item != null)
                this.setText(item.getTitle());
        }
    }
    private class QuestionListCell extends ListCell<Question>
    {
        @Override
        protected void updateItem(Question item, boolean empty)
        {
            super.updateItem(item, empty);

            if(!empty && item != null)
            {
                try
                {
                    GridPane questionView = (GridPane) FXMLLoader.load(Client.this.getClass().getResource("/client/questionView.fxml"));
                    Text numTxt = (Text) questionView.lookup("#numTxt");
                    TextField titleTxt = (TextField) questionView.lookup("#titleTxF");
                    CheckBox requiredCkB = (CheckBox) questionView.lookup("#requiredCkB");
                    TextField hintTxt = (TextField) questionView.lookup("#hintTxF");
                    ComboBox<QuestionType> typeCbo = (ComboBox<QuestionType>) questionView.lookup("#typeCbo");
                    TextField answerAddTextTxF = (TextField) questionView.lookup("#answerAddTextTxF");
                    TextField answerAddValueTxF = (TextField) questionView.lookup("#answerAddValueTxF");
                    Button answerAddBtn = (Button) questionView.lookup("#answerAddBtn");
                    ListView<Answer> answerList = (ListView<Answer>) questionView.lookup("#answerList");
                    Button answerRemoveBtn = (Button) questionView.lookup("#answerRemoveBtn");
                    TextArea answerFreetext = (TextArea) questionView.lookup("#answerFreetext");

                    numTxt.setText(String.format("%d.", 42));
                    titleTxt.setText(item.getTitle());
                    requiredCkB.setSelected(item.isRequired());
                    hintTxt.setText(item.getHint());
                    typeCbo.getItems().addAll(QuestionType.SINGLE, QuestionType.MULTIPLE, QuestionType.FREE);
                    typeCbo.setCellFactory((ListView<QuestionType> param) ->
                    {
                        return new QuestionTypeCell();
                    });
                    typeCbo.setButtonCell(new QuestionTypeCell());
                    typeCbo.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends QuestionType> observable, QuestionType oldValue, QuestionType newValue) ->
                    {
                        item.setType(newValue);
                        switch(newValue)
                        {
                            //TODO Implement different Views
                            case SINGLE:
                                answerAddTextTxF.setVisible(true);
                                answerAddValueTxF.setVisible(true);
                                answerAddBtn.setVisible(true);
                                answerList.setVisible(true);
                                answerRemoveBtn.setVisible(true);
                                answerFreetext.setVisible(false);
                                break;
                            case MULTIPLE:
                                answerAddTextTxF.setVisible(true);
                                answerAddValueTxF.setVisible(true);
                                answerAddBtn.setVisible(true);
                                answerList.setVisible(true);
                                answerRemoveBtn.setVisible(true);
                                answerFreetext.setVisible(false);
                                break;
                            case FREE:
                                answerAddTextTxF.setVisible(false);
                                answerAddValueTxF.setVisible(false);
                                answerAddBtn.setVisible(false);
                                answerList.setVisible(false);
                                answerRemoveBtn.setVisible(false);
                                answerFreetext.setVisible(true);
                                break;
                        }
                        answerList.setCellFactory((ListView<Answer> param) ->
                        {
                            AnswerListCell answerListCell = new AnswerListCell();
                            answerListCell.setType(newValue);
                            return answerListCell;
                        });
                    });
                    typeCbo.setValue(item.getType());
                    answerAddBtn.setOnAction((ActionEvent event) ->
                    {
                        if(!answerAddTextTxF.getText().isEmpty()
                        && !answerAddValueTxF.getText().isEmpty())
                        {
                            try
                            {
                                int value = Integer.parseInt(answerAddValueTxF.getText());
                                Answer answer = new Answer(answerAddTextTxF.getText(), "Test2");
                                item.getAnswers().add(answer);
                                answerList.getItems().add(answer);
                            }
                            catch (NumberFormatException e)
                            {
                                answerAddValueTxF.setText("Ungültiger Wert");
                            }
                        }

                    });
                    answerRemoveBtn.setOnAction((ActionEvent event) ->
                    {
                        if(!answerList.getSelectionModel().isEmpty())
                        {
                            Answer toRemove = answerList.getSelectionModel().getSelectedItem();
                            item.getAnswers().remove(toRemove);
                            answerList.getItems().remove(toRemove);
                        }
                    });

                    this.setGraphic(questionView);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                this.setText(null);
                this.setGraphic(null);
            }
        }
    }
    private class AnswerListCell extends ListCell<Answer>
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
                try
                {
                    switch(type)
                    {
                        case SINGLE:
                            RadioButton radioButton = (RadioButton) FXMLLoader.load(Client.this.getClass().getResource("/client/answer_single.fxml"));
                            radioButton.setText(item.getText());
                            this.setGraphic(radioButton);
                            break;
                        case MULTIPLE:
                            CheckBox checkBox = (CheckBox) FXMLLoader.load(Client.this.getClass().getResource("/client/answer_multiple.fxml"));
                            checkBox.setText(item.getText());
                            this.setGraphic(checkBox);
                            break;
                        case FREE:
                            TextArea textArea = (TextArea) FXMLLoader.load(Client.this.getClass().getResource("/client/answer_free.fxml"));
                            this.setGraphic(textArea);
                            break;
                    }
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
            else
            {
                this.setText(null);
                this.setGraphic(null);
            }
        }
    }
    private class PollStateCell extends ListCell<PollState>
    {
        @Override
        protected void updateItem(PollState item, boolean empty)
        {
            super.updateItem(item, empty);

            if(item != null)
            {
                try
                {
                    Text pollStateTxt = (Text) FXMLLoader.load(Client.this.getClass().getResource("/client/text.fxml"));
                    String txt = "Unbekannt";
                    switch (item)
                    {
                        case NEW: txt = "Neu"; break;
                        case OPEN: txt = "Offen"; break;
                        case CLOESED: txt = "Geschlossen"; break;
                    }
                    pollStateTxt.setText(txt);

                    this.setGraphic(pollStateTxt);
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }
    private class QuestionTypeCell extends ListCell<QuestionType>
    {
        @Override
        protected void updateItem(QuestionType item, boolean empty)
        {
            super.updateItem(item, empty);

            if(item != null)
            {
                try
                {
                    Text pollStateTxt = (Text) FXMLLoader.load(Client.this.getClass().getResource("/client/text.fxml"));
                    String txt = "Unbekannt";
                    switch (item)
                    {
                        case SINGLE: txt = "Einzelauswahl"; break;
                        case MULTIPLE: txt = "Mehrfachauswahl"; break;
                        case FREE: txt = "Freitext"; break;
                    }
                    pollStateTxt.setText(txt);

                    this.setGraphic(pollStateTxt);
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }
}