package syntax;

import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;

import utils.*;
import lexEns.*;

/**
* La classe ActVin met en oeuvre les actions de l'automate d'analyse syntaxique de l'application Vin
*  des fiches de livraison de vin
* 
* @author ?? MERCI DE PRECISER LE NOM DU TRINOME ??
* 
* janvier 2025
*/
public class ActVin extends AutoVin {
	/** indice courant du nombre de chauffeurs dans le tableau tabChauf */
	private int indChaufMax;
	/** indice du chauffeur courant*/
	private int indChauf;
	/** indice courant de la qualité du vin dans le tableau tabIdent */
	private int indQual;
	/** capacité courante */
	private int capacite;
	/** quantités tampon a ajouter une fois que la fiche est correcte */
	private int quantTmpBJ;
	private int quantTmpBG;
	private int quantTmpOR;
	/**nombre de fiche traitées et nombre de fiche correcte */
	private int ficheCorrectes;
	private int ficheTraitees;
	/**chauffeur ayant livré le plus de magasins*/
	private int indBestChauf;
	private int volumeTransporte;
	private TreeSet<Integer> magVisitesTmp;


    /** table des actions */
    private final int[][] ACTION =
    {/* Etat        BJ    BG   IDENT  NBENT VIRG PTVIRG BARRE AUTRES  */
	/* 0 */      { -1,   -1,    1,    -1,   -1,   -1,    8,   -1   },
	/* 1 */      {  3,    4,   11,     2,   -1,   -1,   -1,   -1   },
	/* 2 */      {  3,    4,   11,    -1,   -1,   -1,   -1,   -1   },
	/* 3 */      { -1,   -1,    5,    -1,   -1,   -1,   -1,   -1   },
	/* 4 */      { -1,   -1,   -1,     6,   -1,   -1,   -1,   -1   },
	/* 5 */      { -1,   -1,    5,    -1,   12,    7,   -1,   -1   },
	/* 6 */      { -1,   -1,   -1,    -1,   -1,    9,   10,   -1   },

	
	/* Rappel conventions :  action -1 = action vide, pas de ligne pour etatFinal */
    } ;	       
    
    /** constructeur classe ActVin
	 * @param flot : donnee a analyser
	 */
    public ActVin(InputStream flot) {
    	super(flot);
    }
    
    /** definition de la methode abstraite getAction de Automate 
   	 * 
   	 * @param etat : code de l'etat courant
   	 * @param itemLex : code de l'item lexical courant
   	 * @return code de l'action suivante
   	 **/
   	public int getAction(int etat, int itemLex) {
   		return ACTION[etat][itemLex];
   	}
   	
   	/**
   	 * definition methode abstraite initAction de Automate
   	 */
   	public void initAction() {
   		// Correspond a l'action 0 a effectuer a l'init
   		initialisations();
   	}

   	/** definition de la methode abstraite faireAction de Automate 
   	 * 
   	 * @param etat : code de l'etat courant
   	 * @param itemLex : code de l'item lexical courant
   	 * @return code de l'etat suivant
   	 **/
   	public void faireAction(int etat, int itemLex) {
   		executer(ACTION[etat][itemLex]);
   	}

    /** types d'erreurs detectees */
	private static final int FATALE = 0, NONFATALE = 1;
	
	/** gestion des erreurs 
	 * @param tErr type de l'erreur (FATALE ou NONFATALE)
	 * @param messErr message associe a l'erreur
	 */
	private void erreur(int tErr, String messErr) {
		Lecture.attenteSurLecture(messErr);
		switch (tErr) {
		case FATALE:
			errFatale = true;
			break;
		case NONFATALE:
			etatCourant = etatErreur;
			break;
		default:
			Lecture.attenteSurLecture("parametre incorrect pour erreur");
		}
	}
	
	/**
	 * acces a un attribut lexical 
	 * cast pour preciser que analyseurLexical est ici de type LexVin
	 * @return valEnt associe a l'unite lexicale NBENTIER
	 */
	private int valEnt() {
		return ((LexVin)analyseurLexical).getValEnt();
	}
	/**
	 * acces a un attribut lexical 
	 * cast pour preciser que analyseurLexical est de type LexVin
	 * @return numId associe a l'unite lexicale IDENT
	 */
	private int numIdCourant() {
		return ((LexVin)analyseurLexical).getNumIdCourant();
	}
	
	/** taille d'une colonne pour affichage ecran */
	private static final int MAXLGID = 20;
	/** nombre maximum de chauffeurs */
	private static final int MAXCHAUF = 10;
	/** tableau des chauffeurs et resume des livraison de chacun */
	private Chauffeur[] tabChauf;
	
	
	/** utilitaire d'affichage a l'ecran 
	 * @param ch est une chaine de longueur quelconque
	 * @return chaine ch cadree a gauche sur MAXLGID caracteres
	 * */
	private String chaineCadrageGauche(String ch) {
		int lgch = Math.min(MAXLGID, ch.length());
		String chres = ch.substring(0, lgch);
		for (int k = lgch; k < MAXLGID; k++)
			chres = chres + " ";
		return chres;
	} 
	
	/** affichage de tout le tableau de chauffeurs a l'ecran 
	 * */
	private void afficherChauf() {
		Ecriture.ecrireStringln("");
		String titre = "CHAUFFEUR                   BJ        BG       ORD     NBMAG\n"
				+ "---------                   --        --       ---     -----";
		Ecriture.ecrireStringln(titre);
		for (int i = 0; i <= indChaufMax; i++) {
			String idChaufCourant = ((LexVin)analyseurLexical).chaineIdent(tabChauf[i].numChauf);
			Ecriture.ecrireString(chaineCadrageGauche(idChaufCourant));
			Ecriture.ecrireInt(tabChauf[i].bj, 10);
			Ecriture.ecrireInt(tabChauf[i].bg, 10);
			Ecriture.ecrireInt(tabChauf[i].ordin, 10);
			Ecriture.ecrireInt(tabChauf[i].magDif.size(), 10);
			Ecriture.ecrireStringln("");
		}
	} 
	
	

	
	/**
	 * initialisations a effectuer avant les actions
	 */
	private void initialisations() {
		indChauf = -1;
		indQual = -1;
		quantTmpBG = 0;
		quantTmpBJ = 0;
		quantTmpOR = 0;
		capacite = 100;
		ficheCorrectes = 0;
		ficheTraitees = 0;
		indBestChauf = 0;
		volumeTransporte = 0;
		magVisitesTmp = new TreeSet<Integer>();
		tabChauf = new Chauffeur[MAXCHAUF];

		for (int i = 0; i < MAXCHAUF; i++) {
			tabChauf[i] = new Chauffeur(-1,0,0,0, new TreeSet<Integer>());
		}
	} 
	

	/**
	 * execution d'une action
	 * @param numAct numero de l'action a executer
	 */
	public void executer(int numAct) {
		switch (numAct) {
		case -1:	// action vide
			break;
		case 1 : 
			setIndChauf();
			break;
		case 2 :
			verifCapacite();
			break;
		case 3 : 
			setIndQualBJ();
			break;
		case 4 :
			setIndQualBG();
			break;
		case 5:
			addMagToChauff();
			break;
		case 6 :
			addQtATmp();
			break;
		case 7 :
			incrFicheCor();
			incrFicheTrait();
			ajouterQtTmpChauff();
			afficherChauf();
			verifCapaciteDepassee();
			break;
		case 8 :
			setBestChauf();
			afficherChauf();
			printFichesEtBestChauff();
			break;
		case 9 :
			verifCapaciteDepassee();
			resetQtTMP();
			incrFicheTrait();
			afficherChauf();
			break;
		case 10 :
			incrFicheTrait();
			setBestChauf();
			afficherChauf();
			printFichesEtBestChauff();
			break;
		case 11 :
			addMagToChauff();
			setIndQualOR();
			break;
		case 12 :
			verifCapaciteDepassee();
			break;
		
		default:
			Lecture.attenteSurLecture("action " + numAct + " non prevue");
		}
	}

	/**action 1*/
	private void setIndChauf(){
		int i = 0;
		while (i < MAXCHAUF && tabChauf[i].numChauf != -1) {
			if (tabChauf[i].numChauf == numIdCourant()) {
				indChauf = i;
				return;
			}
			i++;
		}
		if(i >= 10){
			erreur(FATALE, "Nombre de chaffeur max dépassé");
		} else {
			indChaufMax = i;
			tabChauf[i].numChauf = numIdCourant();
			indChauf = i;
		}
	}

	/**action 2*/
	private void verifCapacite(){
		int cap = valEnt();
		if (cap>100 && cap<=200){
			capacite = cap;
		} else {
			System.out.println("Capacité de la citerne de " + ((LexVin)analyseurLexical).chaineIdent(tabChauf[indChauf].numChauf) + "forcée à 100.");
		}
	}

	/**action 3*/
	private void setIndQualBJ(){
		indQual = 0;
	}

	/**action 3*/
	private void setIndQualBG(){
		indQual = 1;
	}

	/**action 11*/
	private void setIndQualOR(){
		indQual = 2;
	}

	/**action 5*/
	private void addMagToChauff(){
		magVisitesTmp.add(numIdCourant());
	}

	/**action 6 (ajoute le nombre lu a la quantité tempon correspondante) */
	private void addQtATmp(){
		volumeTransporte += valEnt();
		if(indQual==0){
			quantTmpBJ += valEnt();
		} else if(indQual == 1){
			quantTmpBG += valEnt();
		} else {
			quantTmpOR += valEnt();
		}
	}

	/**action 7*/
	private void incrFicheCor(){
		ficheCorrectes ++;
	}

	/**action 7 et 9*/
	private void incrFicheTrait(){
		ficheTraitees ++;
	}

	/** action 7*/
	private void ajouterQtTmpChauff(){
		if(quantTmpBG + quantTmpBJ + quantTmpOR == 0){
			erreur(NONFATALE, "Volume transporté egal à 0");
		}
		tabChauf[indChauf].bg += quantTmpBG;
		tabChauf[indChauf].bj += quantTmpBJ;
		tabChauf[indChauf].ordin += quantTmpOR;

		quantTmpBG = 0;
		quantTmpBJ = 0;
		quantTmpOR = 0;

		tabChauf[indChauf].magDif.addAll(Set.copyOf(magVisitesTmp));
		magVisitesTmp.clear();
	}

	/**action 8*/
	private void setBestChauf(){
		int nbMag = 0;
		for (int i = 0; i < tabChauf.length; i++) {
			if (tabChauf[i].magDif.size()>nbMag){
				nbMag = tabChauf[i].magDif.size();
				indBestChauf = i;
			}
		}
	}

	/**action 8 et 10*/
	private void printFichesEtBestChauff(){
		System.out.println("Fiches correctes : " + ficheCorrectes + " - Nombre total de fiches : " + ficheTraitees);
		System.out.println(((LexVin)analyseurLexical).chaineIdent(tabChauf[indBestChauf].numChauf)+" a livré "+tabChauf[indBestChauf].magDif.size()+" magasins différents");
	}

	/**action 9 */
	private void resetQtTMP(){
		quantTmpBG = 0;
		quantTmpBJ = 0;
		quantTmpOR = 0;
		magVisitesTmp.clear();
	}

	/**action 12*/
	private void verifCapaciteDepassee(){
		if(capacite<volumeTransporte){
			erreur(NONFATALE, "Volume transporté supérieur à la capacité");
		}
		volumeTransporte = 0;
	}

}

