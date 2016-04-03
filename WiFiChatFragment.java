package taraprasad73.wordattack;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This fragment handles chat related UI which includes a list view for messages
 * and a message entry field with send button.
 */
public class WiFiChatFragment extends Fragment {

    ChatMessageAdapter adapter = null;
    private final long maxTime = 20000;
    public int turn;
    private int maxTurns = 10;
    private ChatManager chatManager;
    private TextView chatLine;
    private ListView listView;
    private Button sendButton;
    private TextView myScoreText;
    private int myScore;
    private int opponentScore;
    private TextView opponentScoreText;
    private CountDownTimer countDownTimer;
    private TextView countDownTimerText;
    private List<String> items = new ArrayList<String>();
    private int countDownTimeMe;
    private int countDownTimeOpponent;
    private char lastLetter;
    private int wordAttacks = 12;
    private boolean meLostToTime;
    private boolean opponentLostToTime;
    private int numberOfWords;

    public void setOpponentLostToTime(Boolean state) {
        opponentLostToTime = state;
    }
    public void setLastLetter(char lastLetter) {
        this.lastLetter = lastLetter;
    }

    public void showResult() {
        if(meLostToTime){
            chatManager.write("#finished#".getBytes());
            countDownTimerText.setText(R.string.meLossOnTime);
            chatLine.setVisibility(View.GONE);
            sendButton.setVisibility(View.GONE);
            listView.setEnabled(true);
        } else if(opponentLostToTime) {
            countDownTimerText.setText(R.string.opponentLostOnTime);
            chatLine.setVisibility(View.GONE);
            sendButton.setVisibility(View.GONE);
            listView.setEnabled(true);
        }
        else {
            if (myScore > opponentScore)
                countDownTimerText.setText(R.string.result_win);
            else if (opponentScore > myScore)
                countDownTimerText.setText(R.string.result_loss);
            else
                countDownTimerText.setText(R.string.result_tie);
            chatLine.setVisibility(View.GONE);
            sendButton.setVisibility(View.GONE);
            listView.setEnabled(true);
        }
    }

    public void setCountDownTimeOpponent(int countDownTimeOpponent) {
        this.countDownTimeOpponent = countDownTimeOpponent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        chatLine = (TextView) view.findViewById(R.id.txtChatLine);
        sendButton = (Button) view.findViewById(R.id.sendButton);
        myScoreText = (TextView) view.findViewById(R.id.myScoreText);
        opponentScoreText = (TextView) view.findViewById(R.id.opponentScoreText);
        countDownTimerText = (TextView) view.findViewById(R.id.countDownTimerText);
        countDownTimer = new CountDownTimer(maxTime, 1000) {

            public void onTick(long millisUntilFinished) {
                countDownTimeMe = (int) millisUntilFinished / 1000;
                String text = "Time Left: " + countDownTimeMe;
                countDownTimerText.setText(text);
            }

            public void onFinish() {
                meLostToTime = true;
                showResult();
            }
        };
        listView = (ListView) view.findViewById(android.R.id.list);
        //Implement a custom text view
        adapter = new ChatMessageAdapter(getActivity(), android.R.id.text1,
                items);
        listView.setAdapter(adapter);
        listView.setEnabled(false);

        sendButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (chatManager != null) {

                            //sends the string entered to the other device through the chat Managers
                            //write() method which sends the bytes to the socket's output stream
                            if (!chatLine.getText().toString().isEmpty()) {//Excluding Empty Strings
                                String count = String.valueOf(countDownTimeMe);
                                String message = chatLine.getText().toString().toLowerCase();
                                String toSend = chatLine.getText().toString() + "|" + count + "|";

                                if(turn != 0){
                                    if(message.charAt(0)  == lastLetter) {
                                        countDownTimer.cancel();
                                        countDownTimerText.setText("");
                                        chatManager.write(toSend.getBytes());
                                        pushMessage("Me|" + chatLine.getText().toString() + "|");//adds the string into the chat fragment
                                        //by adding the "Me:" tag to it
                                        chatLine.setText("");//Clear the chatLine
                                        //task - try to get the keyboard down
                                        chatLine.clearFocus();//Get the cursor out of the editText chatLine
                                        updateSendButton(false);
                                    }
                                    else{
                                        //Make Toast
                                        Toast toast = Toast.makeText(getActivity(), "Start your word with \""+ lastLetter + "\".", Toast.LENGTH_LONG);
                                        LinearLayout toastLayout = (LinearLayout) toast.getView();
                                        TextView toastTV = (TextView) toastLayout.getChildAt(0);
                                        toastTV.setTextSize(25);
                                        toast.show();
                                    }

                                }
                                else{
                                    chatManager.write(toSend.getBytes());
                                    pushMessage("Me|" + chatLine.getText().toString() + "|");//adds the string into the chat fragment
                                    //by adding the "Me:" tag to it
                                    chatLine.setText("");//Clear the chatLine
                                    //task - try to get the keyboard down
                                    chatLine.clearFocus();//Get the cursor out of the editText chatLine
                                    updateSendButton(false);
                                }
                            }
                        }
                    }
                });
        return view;
    }

    public void updateSendButton(boolean state) {
        sendButton.setClickable(state);
        //task - try to get the keyboard up
        chatLine.requestFocus();
    }

    //Initialization of chat Fragment after the UI Thread receives the message from Chat Fragment itself which itself is created
    //by either the client or the group owner
    public void startCountDownTimer() {
        countDownTimer.start();
    }

    public void setChatManager(ChatManager obj) {
        chatManager = obj;
    }

    private void updateMyScore(String word) {
        myScore += countDownTimeMe;
        if (numberOfWords != 0) {
            int lengthBonus = word.length() * 2;
            myScore += lengthBonus;
        }
            myScoreText.setText("Me: " + myScore);
    }

    private void updateOpponentScore(String word) {
        opponentScore += (int) countDownTimeOpponent;
        if (numberOfWords != 0) {
            int lengthBonus = word.length() * 2;
            opponentScore += lengthBonus;
        }
        opponentScoreText.setText("Buddy: " + opponentScore);
    }

    public void pushMessage(String readMessage) {
        if (readMessage.startsWith("Me|")) {
            updateMyScore(readMessage);
        } else {
            updateOpponentScore(readMessage);
        }
        adapter.add(readMessage);//adds the string message to the array list
        adapter.notifyDataSetChanged();//most probably calls getView method of the
        //adapter for embedding the string message into a TextView
        numberOfWords++;
        if(numberOfWords == maxTurns){
            showResult();
        }
    }

    public interface MessageTarget {
        Handler getHandler();
    }

    /**
     * ArrayAdapter to manage chat messages.
     */
    public class ChatMessageAdapter extends ArrayAdapter<String> {

        List<String> messages = null;

        public ChatMessageAdapter(Context context, int textViewResourceId,
                                  List<String> items) {
            super(context, textViewResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (turn % 2 == 0) {
                    v = vi.inflate(R.layout.custom_list_item1, null);
                    turn++;
                } else {
                    v = vi.inflate(R.layout.custom_list_item2, null);
                    turn++;
                }
            }
            String message = items.get(position);
            if (message != null && !message.isEmpty()) {
                TextView nameText;
                nameText = (TextView) v
                        .findViewById(R.id.text1);
                //android.R.id.text1 is the id
                // of the TextView defined in the android's predefined layout android.layout.simple_list_item1.


                if (nameText != null) {

                    if (message.startsWith("Me|")) {//Apply styles, according to the name of the message creator
                        nameText.setTextAppearance(getActivity(),
                                R.style.myText);
                    } else {//message starts with "Buddy|"
                        nameText.setTextAppearance(getActivity(),
                                R.style.opponentText);
                    }

                }
                //using string tokenizer
                String messageWord;
                String person;
                StringTokenizer st = new StringTokenizer(message, "|");
                person = st.nextToken();
                messageWord = st.nextToken();
                nameText.setText(messageWord.toLowerCase());
                //Adds the text in String format to the textView of the layout
                //Its possible to remove Me and Buddy from the TextView, use the nameText.setText(message)
                //inside the if statements, after removing the Me and Buddy phrases from them.
            }
            return v;
        }
    }
}
