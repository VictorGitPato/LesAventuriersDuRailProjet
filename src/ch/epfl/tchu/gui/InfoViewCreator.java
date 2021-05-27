package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.PlayerId;
import javafx.beans.binding.Bindings;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the view of the information panel
 *
 * @author Anne-Marie (296098)
 */

public class InfoViewCreator {
    private InfoViewCreator() {
    }

    /**
     * Creates the Information Panel on the left side of each player's graphical interface.
     *
     * @param playerId    : the player whose graphical interface this is.
     * @param playerNames : the name of each player.
     * @param gameState   : the state of the game to display.
     * @param infos       : the messages that appear in the bottom-left corner giving information on the sequence of events of the game.
     * @return a Vertical Box containing the messages and each player's stats.
     */
    public static Node createInfoView(PlayerId playerId, Map<PlayerId, String> playerNames, ObservableGameState gameState, ObservableList<Text> infos) {
        VBox infoPane = new VBox();
        infoPane.getStylesheets().addAll("info.css", "colors.css");

        //
        PlayerId currentId = playerId;
        for (int i = 0; i < Menu.number_of_players; i++) {
            infoPane.getChildren().add(playerStats(currentId, playerNames, gameState));
            currentId = currentId.next();
        }

        //
        Separator separator = new Separator();
        infoPane.getChildren().add(separator);

        //
        TextFlow gameInfo = new TextFlow();
        gameInfo.setId("game-info");
        infoPane.getChildren().add(gameInfo);
        Bindings.bindContent(gameInfo.getChildren(), infos);

        //draw cards to be displayed under information
        infoPane.getChildren().add(DecksViewCreator.createDrawnCards(gameState)); //why can only the server see the 3 cards change...

        return infoPane;
    }

    /**
     * Creates the view on each player's stats in the top-left corner of the graphical display.
     *
     * @param playerId    : the player whose graphical interface this is.
     * @param playerNames : the name of each player.
     * @param gameState   : the state of the game to display.
     * @return a Vertical Box with all stats concerning this player and all stats concerning the other player underneath it.
     */
    private static Node playerStats(PlayerId playerId, Map<PlayerId, String> playerNames, ObservableGameState gameState) {
        String playerName = playerNames.get(playerId);
        String playerStyle = playerId.name();
        //
        VBox playerStats = new VBox();
        playerStats.setId("player-stats");
        playerStats.getStyleClass().add(playerStyle);

        Circle circle = new Circle(5);
        circle.getStyleClass().add("filled");

        Text stats = new Text();
        stats.textProperty().bind(Bindings.format(StringsFr.PLAYER_STATS,
                playerName,
                gameState.getTicketCount(playerId),
                gameState.getCardCount(playerId),
                gameState.getCarCount(playerId),
                gameState.getConstructionPoints(playerId)));

        TextFlow textFlow = new TextFlow(circle, stats);
        playerStats.getChildren().addAll(textFlow);

        return playerStats;
    }

    private static Node cards(ObservableGameState gameState, Node cards){
        return cards;
    }

}
