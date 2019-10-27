package game;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.game_objects.Objectif;
import game.game_objects.Obstacle;
import game.game_objects.Pod;
import game.game_objects.SpiritBomb;
import game.interfaces.IGameObject;

/**
 * Une session de jeu.
 *
 */
public class Session {
	/** Table d'association des joueurs.*/
	private Map<String, Player> players;
	
	/** Table d'association des scores.*/
	private Map<String, Integer> scores;
	
	/** L'objectif courant de la session.*/
	private Objectif objectif;
	
	/** La liste des obstacles de la session.*/
	private List<Obstacle> obstacles; 
	
	/** Indique si la session a commencé.*/
	private boolean sessionHasStart;

	/** La liste des bombes posees*/	
	private List<Obstacle> allBombs; 
	
	/**
	 * Constructeur d'une session.
	 */
	public Session() {
		players = new HashMap<>();
		scores = new HashMap<>();
		obstacles = new ArrayList<>();
		allBombs = new ArrayList<>();
		newObjectif();
		sessionHasStart = false;
	}
	
	/**
	 * Vérifie si une session a commencé.
	 * @return True si la session a commencé, false sinon.
	 */
	public boolean sessionHasStart() {
		return sessionHasStart;
	}

	/**
	 * Débute une session.
	 */
	public void startSession() {
		this.sessionHasStart = true;
	}
	
	/**
	 * Termine une session.
	 */
	public void finishSession() {
		this.sessionHasStart = false;
	}

	/**
	 * Ajoute un joueur à la session.
	 * @param pseudo Le pseudo du joueur
	 * @param p Le joueur.
	 */
	public void addPlayer(String pseudo, Player p) {
		players.put(pseudo, p);
		scores.put(pseudo, 0);
	}
	
	/**
	 * Enlève un joueur de la session.
	 * @param pseudo Le pseudo du joueur.
	 */
	public void removePlayer(String pseudo) {
		players.remove(pseudo);
		scores.remove(pseudo);
	}
	
	/**
	 * Vérifie si la session contient un joueur spacifique.
	 * @param pseudo Le pseudo du joueur à vérifier.
	 * @return True si la session contient un joueur avec ce pseudo, false sinon.
	 */
	public boolean containsPseudo(String pseudo) {
		return players.containsKey(pseudo);
	}
	
	/**
	 * Vérifie si la session ne contient aucun joueur.
	 * @return True si la session ne contient aucun joueur, false sinon.
	 */
	public boolean hasNoPlayer() {
		return players.isEmpty();
	}
	
	/**
	 * Récupère les joueurs de la session.
	 * @return Les joueurs de la session.
	 */
	public Map<String, Player> getPlayers() {
		return players;
	}
	
	/**
	 * Récupère un joueur spécifique de la session.
	 * @param playerName Le pseudo du joueur que l'on veut récupèrer.
	 * @return Le joueur que l'on souhaite récupèrer.
	 */
	public Player getPlayer(String playerName) {
		return players.get(playerName);
	}
	
	/**
	 * Retourne le nombre de joueur actuellement dans la session.
	 * @return Le nombre de joueur actuellement dans la session.
	 */
	public int numberOfPlayers() {
		return players.size();
	}
	
	/**
	 * Reset entièrement une session.
	 */
	public void resetSession() {
		// On remet les scores à 0
		for(String key: scores.keySet())
			scores.put(key, 0);
		
		for(String key: players.keySet()) {
			Player previousP = players.get(key);
			Player newP = generateNewPlayer(previousP.getSocket());
			players.put(key, newP);
		}
		
		newObjectif();
		obstacles = new ArrayList<>();
		sessionHasStart = false;
	}
	
	/**
	 * Ajoute 1 au score d'un joueur
	 * @param playerName : joueur qui gagne +1
	 */
	public void addScore(String playerName) {
		int oldScore = scores.get(playerName);
		scores.put(playerName, oldScore + 1);
	}
	
	/**
	 * Retourne les scores de la session sous forme d'une chaine de caractère conforme au protocole.
	 * @return Les scores de la session.
	 */
	public String getScores() {
		String str = "";
		for(String key: scores.keySet()) {
			if (!str.equals(""))
				str += "|";
			str += key + ":" + scores.get(key);
		}
		return str;
	}
	
	/**
	 * Retourne les coordonnées de tous les joueurs de la session  sous forme d'une chaine de caractère conforme au protocole.
	 * @return Les coordonnées de tous les joueurs de la session.
	 */
	public String getCoords() {
		String str = "";
		for(String key: scores.keySet()) {
			if (!str.equals(""))
				str += "|";
			str += key + ":" + players.get(key).getPod().getCoord();
		}
		return str;
	}
	
	/**
	 * Retourne les vecteurs et les angles de tous les joueurs de la session sous forme d'une chaine de caractère conforme au protocole.
	 * @return Le vecteurs et les angles de tous les joueurs de la session. 
	 */
	public String getVcoords() {
		String str = "";
		for(String key: scores.keySet()) {
			if (!str.equals(""))
				str += "|";
			str += key + ":" + players.get(key).getPod().getVcoord();
		}
		return str;
	}
	
	/**
	 * Retourne les coordonnées de tous les obstacles de la session  sous forme d'une chaine de caractère conforme au protocole.
	 * @return Les coordonnées de tous les jobstacles de la session.
	 */
	public String getOcoords() {
		String str = "";
		for(Obstacle o : obstacles) {
			if (!str.equals(""))
				str += "|";
			str += o.getObstacleCoord();
		}
		return str;
	}
	
	/**
	 * @return le nombre de bombes de chaque joueur
	 */
	public String getNbBombs() {
		String str = "";
		
		for(String player : players.keySet()) {
			if (!str.equals(""))
				str += "|";
			str += player+":B"+players.get(player).nbBombes();
		}
		
		return str;
	}
	
	/**
	 * Crée un nouveau joueur avec des coordonnées X/Y aléatoires.
	 * 
	 * @param socket La socket de communication du joueur.
	 * @return Le joueur créé.
	 */
	public Player generateNewPlayer(Socket socket) {
		Pod pod = new Pod(Data.generateRandomX(), Data.generateRandomY());
		// Tant que le nouveau joueur touche un autre objet du jeu, on en créé un autre
		while(touchSomething(pod))
			pod = new Pod(Data.generateRandomX(), Data.generateRandomY());
		
		return new Player(socket, pod, Data.NB_SPIRIT_BOMBS);
	}
	
	/**
	 * Initialise nbObs obstacles dans la session.
	 * @param nbObs Le nombre d'obstacle à créer.
	 */
	public void initializeObstacle(int nbObs) {
		for(int i=0; i<nbObs; i++) {
			Obstacle o = new Obstacle(i);
			while(touchSomething(o))
				o = new Obstacle(i);
			obstacles.add(o);
		}
	}
	
	
	public String getObjectif() {
		return objectif.getObjectifCoord();
	}
	
	/**
	 * Genere de nouvelle coordonnees pour l'objectif.
	 */
	public String newObjectif() {
		objectif = new Objectif();
		while(touchSomething(objectif))
			objectif = new Objectif();
		
		return objectif.getObjectifCoord();
	}
	
	/**
	 * Verifie s'il y a une collision entre objet de jeu, et l'objectif.
	 * @param podPlayer
	 * @return true s'il y a une collision, false sinon.
	 */
	public boolean checkHitObjectif(IGameObject go) {
		if (go != objectif)
			return objectif.checkHit(go);
		else
			return false;
	}
	
	/**
	 * Verifie s'il y a une collision entre un objet du jeu et les joueurs.
	 * @param go : un objet du jeu.
	 * @return Le joueur concerné s'il y a collision, null sinon.
	 */
	public Pod checkHitPlayers(IGameObject go) {
		for(String playerName : players.keySet()) {
			Pod p = players.get(playerName).getPod();
			if (p == go)
				continue;
			if (p.checkHit(go))
				return p;
		}
		return null;
	}
	
	/**
	 * Verifie s'il y a une collision entre un objet du jeu et les obstacles.
	 * @param go : un objet du jeu.
	 * @return L'obstacle concerné s'il y a collision, null sinon.
	 */
	public Obstacle checkHitObstacle(IGameObject go) {
		for(Obstacle o : obstacles) {
			if (o == go)
				continue;
			if(o.checkHit(go))
				return o;
		}
		return null;
	}
	
	/**
	 * Verifie s'il y a une collision entre un objet du jeu et une bombe.
	 * @param go : un objet du jeu.
	 * @return L'obstacle concerné s'il y a collision, null sinon.
	 */
	public Obstacle checkHitBombs(IGameObject go) {		
		for(Obstacle o : allBombs) {
			if(o.checkHit(go)  )
				return o;
		}
		return null;
	}
	
	/**
	 * Ajoute une nouvelle bombe a la liste des bombes presentes sur le terrain
	 * @param player le joueur
	 * @return
	 */
	public Obstacle addBombs(Player player, float x, float y) {
		SpiritBomb bomb = new SpiritBomb(allBombs.size(), player, x, y);
		allBombs.add(bomb);
		
		return bomb;
	}
	
	/**
	 * 
	 * @param idBomb
	 * @return
	 */
	public Obstacle removeBomb(int idBomb) {
		for(int i=0; i<allBombs.size(); i++) {
			if(allBombs.get(i).getId() == idBomb)
				return allBombs.remove(i);
		}
		
		return null;
	}
	
	/**
	 * Vérifie s'il y a une collisions entre un objet de jeu, et les autres déjà instancié
	 * @param go L'objet de jeu
	 * @return True si l'objet de jeu touche un autre objet, false sinon.
	 */
	public synchronized boolean touchSomething(IGameObject go) {

		// On touche un joueur ?
		if (checkHitPlayers(go) != null)
			return true;
		
		// On touche un obstacle ?
		if(checkHitObstacle(go) != null)
			return true;
		
		// On touche l'objectif ?
		if (checkHitObjectif(go))
			return true;
		
		// On ne touche rien !
		return false;
	}
	
	/**
	 * Verifie si un joueur a atteint le win_cap, (s'il a gagne).
	 * @return true s'il a gagne, false sinon.
	 */
	public boolean win(String player) {
		return scores.get(player) == Data.WIN_CAP;
	}
}
