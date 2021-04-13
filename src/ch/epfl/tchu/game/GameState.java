package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.*;

/**
 *Describes the state of the game at a point in time
 * @author Victor Canard-Duchêne (326913)
 * @author Anne-Marie Rusu (296098)
 */
public final class GameState extends PublicGameState{

    private final Map<PlayerId, PlayerState> playerStateMap;

    /**
     * Temporary map for modifications of the current player's state
     */
    private final Map<PlayerId, PlayerState> temporaryMapToModifyPlayerState;
    
    private final Deck<Ticket> ticketDeck;

    private final CardState cardState;


    /**
     * Private constructor for GameState
     * @param playerStates : map with player ids associated to their player states
     * @param ticketDeck : deck of tickets to be drawn throughout the game
     * @param cardState : state of the cards (includes state of the draw & discards pile as well as the face-up cards)
     * @param currentPlayer : id of the player whose turn it is
     * @param lastPlayer : last player to play (unknown until last turn begins)
     */
    private GameState(Map<PlayerId, PlayerState> playerStates,
                      Deck<Ticket> ticketDeck,
                      CardState cardState,
                      PlayerId currentPlayer,
                      PlayerId lastPlayer
                      ) {
        super(ticketDeck.size(), cardState, currentPlayer, makePublic(playerStates), lastPlayer);

        this.playerStateMap = Map.copyOf(playerStates);

        this.ticketDeck = ticketDeck;
        this.cardState = cardState;

        this.temporaryMapToModifyPlayerState = new EnumMap<>(playerStateMap);

    }

    /**
     * Makes the player state map public, ie transforms a map with PlayerState values into a map with PublicPlayerState values
     * @param playerStateMap : map to change the values of
     * @return a new map with player ids associated to public player states
     */
    private static Map<PlayerId, PublicPlayerState> makePublic(Map<PlayerId, PlayerState> playerStateMap){
        Map<PlayerId, PublicPlayerState> publicPlayerStateMap = new EnumMap<>(PlayerId.class);

        publicPlayerStateMap.putAll(playerStateMap);

        return publicPlayerStateMap;
    }

    /**
     * Creates the initial state of the game
     * @param tickets : the group of tickets to be used in the game
     * @param rng : an instance of a random number generator, used to "shuffle" the deck of tickets
     * @return a new GameState representing the initial state of the game
     */
    public static GameState initial(SortedBag<Ticket> tickets, Random rng){
        //Tickets
        Deck<Ticket> ticketDeck = Deck.of(tickets, rng);

        //PlayerStateMap
        Map<PlayerId, PlayerState> playerStateMap = new EnumMap<>(PlayerId.class);

        //Initial deck
        Deck<Card> initialDeck = Deck.of(Constants.ALL_CARDS, rng);

        //Initializes each player's deck to the top 4 cards of the deck (and then the 4 next)
        for (PlayerId playerId : PlayerId.values()){
            SortedBag<Card> top4Cards = initialDeck.topCards(Constants.INITIAL_CARDS_COUNT);
            initialDeck = initialDeck.withoutTopCards(Constants.INITIAL_CARDS_COUNT);

            PlayerState playerState = PlayerState.initial(top4Cards);
            //Initializes a new PlayerState with these cards and places it in the map
            playerStateMap.put(playerId, playerState);
        }

        Deck<Card> cardDeck = initialDeck;
        //Deck without the top 8 cards
        CardState cardState = CardState.of(cardDeck);
        //Makes a CardState of that initial deck (5 face-up cards, a draw-pile and an empty discard pile)

        int firstPlayerIndex = rng.nextInt(PlayerId.COUNT);
        //Picks a player at random
        PlayerId firstPlayerId = PlayerId.ALL.get(firstPlayerIndex);

        return new GameState(playerStateMap, ticketDeck, cardState, firstPlayerId, null);
    }

    /**
     * Overrides PublicGameState's method as it returns the private part of the player state
     * @param playerId the player which we want to know the state of
     * @return the private player state associated to the player id given as argument
     */
    @Override
    public PlayerState playerState(PlayerId playerId){return playerStateMap.get(playerId);}

    /**
     * Overrides PublicGameState's method as it returns the private part of the current player state
     * @return the current player's private state
     */
    @Override
    public PlayerState currentPlayerState(){return playerStateMap.get(super.currentPlayerId());}

    //Group 1

    /**
     * Returns the count number of tickets from the top of the pile
     * @param count : number of tickets
     * @throws IllegalArgumentException if the count is negative or strictly superior to the player's number of tickets
     * @return the count number of top tickets
     */
    public SortedBag<Ticket> topTickets(int count){
        Preconditions.checkArgument(count >= 0 && count <= ticketsCount());

        return ticketDeck.topCards(count);
    }

    /**
     * Return a new game state without the count number of tickets
      * @param count : number of tickets we don't want in this new GameState
     * @throws IllegalFormatCodePointException if the count is negative or strictly superior to the player's number of tickets
     * @return a new game state with count tickets removed
     */
    public GameState withoutTopTickets(int count){
        Preconditions.checkArgument(count >= 0 && count <= ticketsCount());

        return new GameState(playerStateMap, ticketDeck.withoutTopCards(count), cardState, super.currentPlayerId(), super.lastPlayer());
    }

    /**
     * Returns the top card of the cardState
     * @throws IllegalArgumentException if the deck is empty
     * @return the card at the top
     */
    public Card topCard(){
        Preconditions.checkArgument(!cardState.isDeckEmpty());

        return cardState.topDeckCard();
    }

    /**
     * Returns a GameState without the top card
     * @throws IllegalArgumentException if the deck is empty
     * @return a new GameState without the top card
     */
    public GameState withoutTopCard(){
        Preconditions.checkArgument(!cardState.isDeckEmpty());

        return new GameState(playerStateMap, ticketDeck, cardState.withoutTopDeckCard(),super.currentPlayerId(), super.lastPlayer());
    }

    /**
     * Makes a new GameState with more cards included in the discard pile
     * @param discardedCards : cards to add to card's state discard pile
     * @return a new game state with more discarded cards
     */
    public GameState withMoreDiscardedCards(SortedBag<Card> discardedCards){
        return new GameState(playerStateMap, ticketDeck, cardState.withMoreDiscardedCards(discardedCards), super.currentPlayerId(), super.lastPlayer());
    }

    /**
     * Returns an identical GameState except if the draw pile is empty in which case it creates a new from the discards pile and shuffles it
     * @param rng : Random Number Generator to shuffle the new deck created from the discard pile
     * @return identical game state or with a new draw pile made from the discards pile.
     */
    public GameState withCardsDeckRecreatedIfNeeded(Random rng){
        if(cardState.isDeckEmpty()){
            return new GameState(playerStateMap, ticketDeck, cardState.withDeckRecreatedFromDiscards(rng), super.currentPlayerId(), super.lastPlayer());
        }
        return this;
    }
//Group 2

    /**
     * Returns an identical GameState but with the player's initially chosen tickets added
     * @param playerId : Player 1 or 2, the player that has chosen these tickets
     * @param chosenTickets : tickets chosen at the start of the game
     * @throws IllegalArgumentException if the player has at least one ticket already
     * @return new GameState with added tickets for the player state associated to the param player id
     */
    public GameState withInitiallyChosenTickets(PlayerId playerId, SortedBag<Ticket> chosenTickets){
        PlayerState playerStateToModify = playerStateMap.get(playerId);
        Preconditions.checkArgument(playerStateToModify.tickets().isEmpty());

        temporaryMapToModifyPlayerState.put(playerId, playerStateToModify.withAddedTickets(chosenTickets));

        return new GameState(temporaryMapToModifyPlayerState, ticketDeck, cardState, super.currentPlayerId(), super.lastPlayer());
    }

    /**
     * Returns a new GameState with less tickets in the ticket pile as these are now in the player's state
     * @param drawnTickets : all tickets drawn by the player initially
     * @param chosenTickets : tickets kept by the player
     * @throws IllegalArgumentException if drawn tickets does not contain the chosen tickets
     * @return new GameState with the current player's chosen additional tickets
     */
    public GameState withChosenAdditionalTickets(SortedBag<Ticket> drawnTickets, SortedBag<Ticket> chosenTickets){
        Preconditions.checkArgument(drawnTickets.contains(chosenTickets));

        temporaryMapToModifyPlayerState.put(super.currentPlayerId(), currentPlayerState().withAddedTickets(chosenTickets));

        return new GameState(temporaryMapToModifyPlayerState, ticketDeck.withoutTopCards(drawnTickets.size()), cardState, super.currentPlayerId(), super.lastPlayer());
    }

    /**
     * Returns a new GameState where the chosen card at index slot was taken from the face-up cards and put into the player's hand
     * And the chosen card in the face up cards was replaced with the one at the top of the deck
     * @param slot : the index of the drawn face-up
     * @throws IllegalArgumentException if it's not possible to draw cards
     * @return a new GameState with a drawn face up card at a given slot
     */
    public GameState withDrawnFaceUpCard(int slot){
        Preconditions.checkArgument(canDrawCards());

        Card cardToAdd = cardState.faceUpCard(slot);
        temporaryMapToModifyPlayerState.put(super.currentPlayerId(), currentPlayerState().withAddedCard(cardToAdd));

        return new GameState(temporaryMapToModifyPlayerState, ticketDeck, cardState.withDrawnFaceUpCard(slot), super.currentPlayerId(), super.lastPlayer());
    }

    /**
     * Returns a new GameState where the deck's top card has been placed in the current player's hand
     * @throws IllegalArgumentException if it's not possible to draw cards
     * @return a new modified GameState with a blindly drawn card
     */
    public GameState withBlindlyDrawnCard(){
        Preconditions.checkArgument(canDrawCards());

        Card cardOnTopOfTheDeck = cardState.topDeckCard();

        temporaryMapToModifyPlayerState.put(super.currentPlayerId(), currentPlayerState().withAddedCard(cardOnTopOfTheDeck));

        return new GameState(temporaryMapToModifyPlayerState, ticketDeck, cardState.withoutTopDeckCard(), super.currentPlayerId(), super.lastPlayer());
    }

    /**
     * Returns a new GameState where the current player has claimed a given route with a given set cards
     * (thus adds the route to the player's collection and removes the cards he used)
     * @param route : the claimed route
     * @param cards : the claim cards
     * @return a new GameState with a new route and less cards for the current player
     */
    public GameState withClaimedRoute(Route route, SortedBag<Card> cards){
        temporaryMapToModifyPlayerState.put(super.currentPlayerId(), currentPlayerState().withClaimedRoute(route, cards));
        CardState newState = cardState.withMoreDiscardedCards(cards);

        return new GameState(temporaryMapToModifyPlayerState, ticketDeck, newState, super.currentPlayerId(), super.lastPlayer());
    }

    //Group 3

    /**
     * Determines when the last turn of the game begins
     * @return true if the last player is not already known and if the current player has at most 2 cars left
     */
    public boolean lastTurnBegins(){
        boolean lastPlayerIsUnknown = (super.lastPlayer() == null);

        int numberOfCars = super.currentPlayerState().carCount();
        boolean onlyTwoWagonsLeftOrLess = numberOfCars <= 2;

        return lastPlayerIsUnknown && onlyTwoWagonsLeftOrLess;
    }

    /**
     *The state of the game where it's the next player's turn to play.
     * Changes the last player's id if the last turn begins.
     * @return a new GameState where it's the next player's turn
     */
    public GameState forNextTurn(){
        PlayerId lastPlayer = (lastTurnBegins()) ? super.currentPlayerId() : super.lastPlayer();
        PlayerId otherPlayer = super.currentPlayerId().next();

        return new GameState(playerStateMap, ticketDeck, cardState, otherPlayer, lastPlayer);
    }

}
