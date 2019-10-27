package game;

import java.net.Socket;

import game.game_objects.Pod;

/**
 * Classe représentant un joueur.
 *
 */
public class Player {
	/** La socket de communication du joueur.*/
	private Socket socket;
	
	/** Le pod du joueur.*/
	private Pod pod;
	
	/** Les bombes que possède un joueur **/
	private int nbSpiritBombs;
	
	/**
	 * Constructeur d'un joueur.
	 * @param socket Sa socket de communication.
	 * @param pod Son pod.
	 * @param nbBombs nombre de spirit bombs que possede le joueur au depart
	 */
	public Player(Socket socket, Pod pod, int nbBombs) {
		this.socket = socket;
		this.pod = pod;
		this.nbSpiritBombs = nbBombs;
	}
	
	/**
	 * Getteur sur la socket du joueur.
	 * @return La socket du joueur.
	 */
	public Socket getSocket() {
		return socket;
	}
	
	/**
	 * Getteur sur le pod du joueur.
	 * @return Le pod du joueur.
	 */
	public Pod getPod() {
		return pod;
	}
	
	/**
	 * Met à jour les information du pod à partir des commandes donnée en paramètre.
	 * @param angleCmd L'ajout à l'angle.
	 * @param thrustCmd Le nombre de poussé effectuée.
	 */
	public void updateFromCommand(float angleCmd, int thrustCmd) {
		pod.updateFromCommand(angleCmd, thrustCmd);
	}
	
	/**
	 * Met à jour la position du joueur à partir de son vecteur et de son angle.
	 */
	public void updatePosition() {
		pod.updatePosition();
	}
	
	/**
	 * Ajoute une bombe au stock de bombes du joueur
	 */
	public void addBomb() {
		this.nbSpiritBombs++;
	}
	
	/**
	 * Enleve une bombe au stock de bombes du joueur
	 */
	public boolean removeBomb() {
		if(nbSpiritBombs == 0) {
			return false;
		}else {
			this.nbSpiritBombs--;
			return true;
		}
	}
	
	/**
	 * @return le nombre de bombes que possede un joueur
	 */
	public int nbBombes() {
		return nbSpiritBombs;
	}
}
