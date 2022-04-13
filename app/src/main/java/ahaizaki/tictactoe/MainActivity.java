package ahaizaki.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import java.util.Random;

import ahaizaki.tictactoe.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private int turn;
    private String[] cells = {"","","","","","","","",""};
    private final String[] playerIcons = {"x","o"};
    private boolean isFinished = false;
    private int toRestore = 0;
    private String textViewMessage;
    private int spinnerPosition = 1;
    ImageButton[] buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupMenu();
        setupListener();
        initGame();
        if(savedInstanceState != null) {
            restore(savedInstanceState);
        }
    }

    private void setupMenu() {
        ArrayAdapter<CharSequence>adapter= ArrayAdapter.createFromResource(this, R.array.Difficulty, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        binding.spinner.setAdapter(adapter);
    }

    private void restore(@NonNull Bundle savedInstanceState) {
        spinnerPosition = savedInstanceState.getInt("spinnerPosition");
        binding.spinner.setSelection(spinnerPosition);
        turn = savedInstanceState.getInt("turn");
        setTurn(turn);
        isFinished = savedInstanceState.getBoolean("isFinished");
        textViewMessage = savedInstanceState.getString("textViewMessage");
        cells = savedInstanceState.getStringArray("cells");
        binding.textView.setText(textViewMessage);
        for (int i = 0; i < cells.length; i++) {
            if(!cells[i].isEmpty()) {
                buttons[i].setImageResource((cells[i].equals("x")) ?
                        R.mipmap.player1_icon_round : R.mipmap.player2_icon_round);
            } else {
                buttons[i].setImageResource(R.mipmap.background);
            }
        }
        toRestore = 2;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("turn",turn);
        outState.putBoolean("isFinished",isFinished);
        outState.putStringArray("cells",cells);
        outState.putString("textViewMessage",textViewMessage);
        outState.putInt("spinnerPosition",spinnerPosition);
        super.onSaveInstanceState(outState);
    }

    private void setupListener() {
        buttons = new ImageButton[]{binding.cell1, binding.cell2, binding.cell3,
        binding.cell4, binding.cell5, binding.cell6,
        binding.cell7, binding.cell8, binding.cell9};

        binding.reset.setOnClickListener(view -> resetGame());
        for (int i = 0; i < buttons.length; i++) {
            int cellNumber = i;
            buttons[i].setOnClickListener(view -> checkCell(cellNumber,buttons[cellNumber]));
        }
        binding.spinner.setSelection(spinnerPosition);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if(toRestore == 0) {
                        spinnerPosition = i;
                        resetGame();
                    } else {
                        toRestore--;
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initGame() {

        setTurn(new Random().nextInt(2));
        if(turn == 1 && spinnerPosition != 3) {
            int cellNumber = new Random().nextInt(9);
            fillCell(cellNumber,buttons[cellNumber]);
        }
    }

    private void resetGame() {
        for (int i = 0; i < buttons.length; i++) {
            cells[i] = "";
            buttons[i].setImageResource(R.mipmap.background);
        }
        isFinished = false;
        initGame();
    }

    private void checkCell(int cellNumber,ImageButton button) {
        if(cells[cellNumber].isEmpty() && !isFinished) {
            fillCell(cellNumber,button);
        }
    }

    private void fillCell(int cellNumber,ImageButton button) {
        cells[cellNumber] = playerIcons[turn];
        button.setImageResource((turn == 0) ? R.mipmap.player1_icon_round : R.mipmap.player2_icon_round);
        if(detectWin(cellNumber)) {
            finishGame(true);
        } else if(hasEmptyCell()) {
            finishGame(false);
        } else{
            setTurn((turn == 0) ? 1 : 0);
            if(turn == 1 && spinnerPosition != 3) {
                enemyChoice();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void setTurn(int turn) {
        this.turn = turn;
        textViewMessage = "Turno del\n jugador " + (turn + 1);
        binding.textView.setText(textViewMessage);
    }

    private boolean checkNormalLine(int row) {
        return cells[row * 3].equals(cells[row * 3 + 1])
                && cells[row * 3].equals(cells[row * 3 + 2]) && !cells[row * 3].isEmpty();
    }

    private boolean checkVerticalLine(int column) {
        return cells[column].equals(cells[column + 3])
                && cells[column].equals(cells[column + 6]) && !cells[column].isEmpty();
    }

    private boolean checkDiagonalLine() {
        return (cells[0].equals(cells[4])
                && cells[0].equals(cells[8]) && !cells[0].isEmpty()) ||
                (cells[2].equals(cells[4]) && cells[2].equals(cells[6]) && !cells[2].isEmpty());
    }

    @SuppressLint("SetTextI18n")
    private void finishGame(boolean hasWin) {
        isFinished = true;
        if(hasWin) {
            textViewMessage = "Jugador " + (turn + 1) + "\nha ganado";
        } else {
            textViewMessage = "Empate";
        }
        binding.textView.setText(textViewMessage);
    }

    private boolean hasEmptyCell() {
        int emptyCells = 0;
        for (String cell: cells) {
            if(cell.isEmpty()) {
                emptyCells++;
            }
        }
        return emptyCells == 0;
    }

    int bestChoice(int cellNumber,int argTurn, int alpha, int beta) {
        int score;
        if (detectWin(cellNumber)) {
            if(spinnerPosition < 2) {
                return argTurn == 1 ? 1 : -1;
            } else {
                return argTurn == 1 ? -1 : 1;
            }
        } else if (hasEmptyCell()) {
            return 0;
        } else {
            for (int i = 0; i < cells.length; i++) {
                if (cells[i].isEmpty()) {
                    cells[i] = playerIcons[spinnerPosition == 0 ? argTurn == 1 ? 0 : 1 : argTurn];
                    score = bestChoice(i,argTurn == 1 ? 0 : 1, alpha, beta);
                    cells[i] = "";
                    if (argTurn == 1) {
                        alpha = Math.max(alpha, score);
                        if (alpha >= beta) {
                            return beta;
                        }
                    } else {
                        beta = Math.min(beta, score);
                        if (beta < alpha) {
                            return alpha;
                        }
                    }
                }
            }
            if (argTurn == 1)
                return alpha;
            else
                return beta;
        }
    }

    private boolean detectWin(int cellNumber) {
        return checkNormalLine(cellNumber / 3) || checkDiagonalLine()
                || checkVerticalLine(cellNumber % 3);
    }

    private void enemyChoice() {
        int best = 0;
        int bestScore = -1000;
        int score;
        for (int i = 0; i < cells.length; i++) {
            if (cells[i].isEmpty()) {
                cells[i] = playerIcons[1];
                score = bestChoice(i,0, bestScore, 1000);
                if (score > bestScore) {
                    bestScore = score;
                    best = i;
                }
                cells[i] = "";
            }
        }
        checkCell(best,buttons[best]);
    }


}