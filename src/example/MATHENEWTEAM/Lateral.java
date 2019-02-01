package example.MATHENEWTEAM;

import java.util.ArrayList;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.EMatchState;
import simple_soccer_lib.utils.Vector2D;

public class Lateral extends Thread {
	private static final double ERROR_RADIUS = 1.0d;
	
	private enum State { ATTACKING, RETURN_TO_HOME, BLOCKING, WAITING, CORN_KICK };

	private PlayerCommander commander;
	private State state;
	
	private PlayerPerception selfInfo;
	private FieldPerception  fieldInfo;
	private MatchPerception  matchInfo;
	
	private Vector2D homebase; //posição base do jogador
	
	private Vector2D goalPosition;
	private Vector2D attackPosition;
	
	private int[] numerosCamisa = {0,0,0,0,0,0,0};	
	private boolean flag;
	
	//variáveis para a "troca de lados"
	private int[] xUniformNumber = {-52,-25,-10,-5};
	private int xBlocking = -10;
	private int xAttacking = 26;
	private int xAttackPosition = 32;
		
	public Lateral(PlayerCommander player, double x, double y, double yAttack) {
		commander = player;
		homebase = new Vector2D(x, y);
		flag = true;
		goalPosition = new Vector2D(52, 0);
		attackPosition = new Vector2D(xAttackPosition, yAttack);
	}
	
	private void getCamisa(){
		flag = false;
		ArrayList<PlayerPerception> players = new ArrayList<PlayerPerception>();
		players.addAll(fieldInfo.getTeamPlayers(selfInfo.getSide()));
				
		for(PlayerPerception p:players){
			if(arrivedAtAt(new Vector2D(xUniformNumber[0],0), p.getPosition())){				
				numerosCamisa[0] = p.getUniformNumber();
				System.out.println(p.getUniformNumber());
			}else if(arrivedAtAt(new Vector2D(xUniformNumber[1],20), p.getPosition())){				
				numerosCamisa[1] = p.getUniformNumber();	
				System.out.println(p.getUniformNumber());
			}else if(arrivedAtAt(new Vector2D(xUniformNumber[1],-20), p.getPosition())){				
				numerosCamisa[2] = p.getUniformNumber();
				System.out.println(p.getUniformNumber());
			}else if(arrivedAtAt(new Vector2D(xUniformNumber[1],0), p.getPosition())){				
				numerosCamisa[3] = p.getUniformNumber();
				System.out.println(p.getUniformNumber());
			}else if(arrivedAtAt(new Vector2D(xUniformNumber[2],0), p.getPosition())){				
				numerosCamisa[4] = p.getUniformNumber();	
				System.out.println(p.getUniformNumber());
			}else if(arrivedAtAt(new Vector2D(xUniformNumber[3],28), p.getPosition())){				
				numerosCamisa[5] = p.getUniformNumber();	
				System.out.println(p.getUniformNumber());
			}else if(arrivedAtAt(new Vector2D(xUniformNumber[3],-28), p.getPosition())){				
				numerosCamisa[6] = p.getUniformNumber();	
				System.out.println(p.getUniformNumber());
			}else{
				System.out.println("Lateral ----- Isao Takahata"+ p.getUniformNumber());
			}			
		}
	}
	
	private boolean arrivedAtAt(Vector2D targetPosition, Vector2D agentPosition) {
		//Vector2D myPos = selfInfo.getPosition();
		return Vector2D.distance(agentPosition, targetPosition) <= ERROR_RADIUS;
	}
	
	@Override
	public void run() {
		_printf("Waiting initial perceptions...");
		selfInfo  = commander.perceiveSelfBlocking();
		fieldInfo = commander.perceiveFieldBlocking();
		matchInfo = commander.perceiveMatchBlocking();
		
		state = State.RETURN_TO_HOME; //todos começam neste estado
 
		if (selfInfo.getSide() == EFieldSide.RIGHT) { //ajusta a posição base de acordo com o lado do jogador (basta mudar o sinal do x)
			swapSides();
			commander.doMoveBlocking((homebase.getX()*-1), homebase.getY());
		}
		commander.doMoveBlocking(homebase.getX(), homebase.getY());
		
		try {
			Thread.sleep(5000); // espera, para dar tempo de ver as mensagens iniciais
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		while (commander.isActive()) {
			updatePerceptions();  //deixar aqui, no começo do loop, para ler o resultado do 'move'
			
			if(flag) getCamisa();
			
			//_printf(" "+state);
			
			if (matchInfo.getState() == EMatchState.PLAY_ON) {			
				switch (state) {
				case ATTACKING:
					//System.out.println("ATACKING");
					stateAttacking();
					break;
				case RETURN_TO_HOME:
					//System.out.println("RETURN HOME");
					stateReturnToHomeBase();
					break;
				case BLOCKING:
					//System.out.println("BLOCKING");
					stateBlocking();
					break;
				default:
					_printf("Invalid state: %s", state);
					break;	
				}			
			}else if(matchInfo.getState() == EMatchState.CORNER_KICK_LEFT ){
				//state = State.CORN_KICK;
				stateCornerKick();
				//return;
			}else if(matchInfo.getState() == EMatchState.AFTER_GOAL_LEFT || matchInfo.getState() == EMatchState.AFTER_GOAL_RIGHT )
			{
				commander.doMoveBlocking(homebase.getX(), homebase.getY());
			}
			
			else if(matchInfo.getState() == EMatchState.FREE_KICK_LEFT )
			{
				stateFault();
			}
			else if(matchInfo.getState() == EMatchState.FREE_KICK_FAULT_LEFT )
			{
				stateFault();
			}
			else if(matchInfo.getState() == EMatchState.GOAL_KICK_LEFT || matchInfo.getState() == EMatchState.GOAL_KICK_RIGHT )
			{
				commander.doMoveBlocking(homebase.getX(), homebase.getY());
			}
		}			
	}
	
	private void updatePerceptions() {
		PlayerPerception newSelf = commander.perceiveSelf();
		FieldPerception newField = commander.perceiveField();
		MatchPerception newMatch = commander.perceiveMatch();
		
		// só atualiza os atributos se tiver nova percepção (senão, mantém as percepções antigas)
		if (newSelf != null) {
			this.selfInfo = newSelf;
		}
		if (newField != null) {
			this.fieldInfo = newField;
		}
		if (newMatch != null) {
			this.matchInfo = newMatch;
		}
	}
	
	private void swapSides(){
		for(int i=0;i<xUniformNumber.length;i++){
			xUniformNumber[i] = xUniformNumber[i]*(-1);
		}
		goalPosition.setX(goalPosition.getX()*(-1));
		xBlocking = xBlocking*(-1);
		homebase.setX(homebase.getX()*(-1));
		xAttacking = xAttacking*(-1);
		xAttackPosition = xAttackPosition*(-1);
	}
	
	/////// Estado CORNER_KICK /////// ------------>>>>>>>>>>>>>>>>>>corrigir este estado - não entra aqui nunca!
	private void stateCornerKick(){
		Vector2D ballPosition = fieldInfo.getBall().getPosition();
		Vector2D attackPosition = fieldInfo.getTeamPlayer(selfInfo.getSide(), 5).getPosition();
		if(isMySide()){
			if (arrivedAt(ballPosition)) {
				commander.doKickToPoint(100, attackPosition);
				state = State.ATTACKING;			
			} else {
				if (isAlignedTo(ballPosition)) {
					//_printf("ATK: Running to the ball...");
					commander.doDashBlocking(100.0d);
				} else {
					//_printf("ATK: Turning...");
					turnTo(ballPosition);
				}
			}	
		}
	}
	
	private void stateFault()
	{
		Vector2D ballPosition = fieldInfo.getBall().getPosition();
		Vector2D attackPosition = fieldInfo.getTeamPlayer(selfInfo.getSide(), 5).getPosition();
		if(isMySide()){
			if (arrivedAt(ballPosition)) {
				commander.doKickToPoint(100, attackPosition);
				state = State.ATTACKING;			
			} else {
				if (isAlignedTo(ballPosition)) {
					//_printf("ATK: Running to the ball...");
					commander.doDashBlocking(100.0d);
				} else {
					//_printf("ATK: Turning...");
					turnTo(ballPosition);
				}
			}	
		}
		
	}
	
	/////// Estado Blocking ///////
	private void stateBlocking(){
		Vector2D ballPosition = fieldInfo.getBall().getPosition();
		int proximo = closerToTheball();
		//Vector2D playerPosition = selfInfo.getPosition();
	//	System.out.println(selfInfo.getUniformNumber()+">>>>>>>>>>>>>>>>>>>>>>>Blocking");
		
		if(ballPosition.getX() < xBlocking){
			state = State.RETURN_TO_HOME;
		//	System.out.println(selfInfo.getUniformNumber()+">>>>>>"+"Blocking>>>>>>>>>>>>>>>Return to home;");
			return;
		}
		if(teamHasBall()){
			state = State.ATTACKING;
			//System.out.println(selfInfo.getUniformNumber()+">>>>>>"+"Blocking>>>>>>>>>>>>>>>Attacking");
			return;
		}
		
		if(isMySide() && !teamHasBall()){
			System.out.println("ENTRA AQUI PELO AMOR DE JEOVA");
			if (arrivedAt(ballPosition)) {
				Vector2D playerPosition = fieldInfo.getTeamPlayer(selfInfo.getSide(), 5).getPosition();
				double distance = 100*(selfInfo.getPosition().distanceTo(playerPosition)/40);
				commander.doKickToPointBlocking(distance, playerPosition);
				state = State.RETURN_TO_HOME;
				//System.out.println(selfInfo.getUniformNumber()+">>>>>>"+"Blocking>>>>>>>222222222>>>>>>>>Return to home");
				return;
			} else {
				if (isAlignedTo(ballPosition)) {
					//_printf("ATK: Running to the ball...");
					commander.doDashBlocking(100.0d);				
				} else {
					//_printf("ATK: Turning...");
					turnTo(ballPosition);
				}
			}		
		}
		
	}
	
	private int closerToTheGoal(){
		//System.out.println("ENTROU NO COLSER TO THE GOAL");
		double d = 0, k=0;
		int res = 0;
		d = Vector2D.distance(fieldInfo.getTeamPlayer(selfInfo.getSide(), numerosCamisa[0]).getPosition(),(goalPosition));
		for(int i=1;i<numerosCamisa.length;i++){
			k = Vector2D.distance(fieldInfo.getTeamPlayer(selfInfo.getSide(), i).getPosition(),(goalPosition));
			if(d > k){
				d = k;
				res = i;
			}
		}
		return res;
	}
	
	private int closerToTheball(){
		//System.out.println("ENTROU NO COLSER TO THE GOAL");
		Vector2D ballposition = fieldInfo.getBall().getPosition();
		double d = 0, k=0;
		int res = 0;
		d = Vector2D.distance(fieldInfo.getTeamPlayer(selfInfo.getSide(), numerosCamisa[0]).getPosition(),(ballposition));
		for(int i=1;i<numerosCamisa.length;i++){
			k = Vector2D.distance(fieldInfo.getTeamPlayer(selfInfo.getSide(), i).getPosition(),(ballposition));
			if(d > k){
				d = k;
				res = i;
			}
		}
		return res;
	}
	
	private boolean withMyTeam(){
		ArrayList<PlayerPerception> players = new ArrayList<PlayerPerception>();
		Vector2D ballPosition = fieldInfo.getBall().getPosition();
		//players = fieldInfo.getTeamPlayers(selfInfo.getSide()==EFieldSide.LEFT?EFieldSide.RIGHT:EFieldSide.LEFT);
		players = fieldInfo.getTeamPlayers(selfInfo.getSide());
		for(PlayerPerception p: players){
			if(arrivedAtAt(p.getPosition(), ballPosition)){
				////System.out.println("----------------------------------------->Out");
				return true;				
			}
		}
		return false;
	}
	
	private boolean isMySide(){ //verifica se a bola está na metade do campo que o jogador está
		if(((selfInfo.getPosition().getY() > 0)&&(fieldInfo.getBall().getPosition().getY() > 0))||
				((selfInfo.getPosition().getY() <= 0)&&(fieldInfo.getBall().getPosition().getY() <= 0))){
			return true;
		}
		return false;
	}

	/////// estado RETURN_TO_HOMEBASE ///////
	private void stateReturnToHomeBase() {
		
		if(!teamHasBall()){
			state = State.BLOCKING;
			return;
		}
		
		/*
		 * if(selfInfo.getSide().value() == 1) {
		 * if(fieldInfo.getBall().getPosition().getX() > 0){ state = State.ATTACKING;
		 * return; }
		 * 
		 * }else if(selfInfo.getSide().value() == - 1) {
		 * if(fieldInfo.getBall().getPosition().getX() < 0){ state = State.ATTACKING;
		 * return; } }
		 */
		if(teamHasBall())
		{
			state = State.ATTACKING;
			return;
		}
				
		if (! arrivedAt(homebase)) {			
			if (isAlignedTo(homebase)) {
				//_printf("RTHB: Running to the base...");
				commander.doDash(100.0d);			
			} else {
				//_printf("RTHB: Turning...");
				turnTo(homebase);
			}			
		}	
	}

	private boolean arrivedAt(Vector2D targetPosition) {
		Vector2D myPos = selfInfo.getPosition();
		return Vector2D.distance(myPos, targetPosition) <= ERROR_RADIUS;
	}

	private void turnTo(Vector2D targetPosition) {
		Vector2D myPos = selfInfo.getPosition();
		////System.out.println(" => Target = " + targetPosition + " -- Player = " + myPos);
		
		Vector2D newDirection = targetPosition.sub(myPos);
		
		commander.doTurnToDirectionBlocking(newDirection);
	}
	
	private boolean isAlignedTo(Vector2D targetPosition) {
		Vector2D myPos = selfInfo.getPosition();
		if (targetPosition == null || myPos == null) {
			return false;			
		}
		double angle = selfInfo.getDirection().angleFrom(targetPosition.sub(myPos));
		return angle < 15.0d && angle > -15.0d;
	}
	
	private double pointDistance(Vector2D player, Vector2D ball){
		double termX = player.getX() - ball.getX();
		double termY = player.getY() - ball.getY();
		return Math.sqrt((termX*termX)+(termY*termY));
	}
	
	private boolean teamHasBall() {
		double smallerDistanceMyTeam = 2000;
		double smallerDistanceOtherTeam = 2000;
		if(this.selfInfo.getSide().equals(EFieldSide.LEFT)) {			
			for(PlayerPerception p: this.fieldInfo.getTeamPlayers(EFieldSide.LEFT)) {
				if(p.getPosition().distanceTo(this.fieldInfo.getBall().getPosition()) < smallerDistanceMyTeam)
					smallerDistanceMyTeam = p.getPosition().distanceTo(this.fieldInfo.getBall().getPosition());
			}
			for(PlayerPerception p: this.fieldInfo.getTeamPlayers(EFieldSide.RIGHT)) {
				if(p.getPosition().distanceTo(this.fieldInfo.getBall().getPosition()) < smallerDistanceMyTeam)
					smallerDistanceOtherTeam = p.getPosition().distanceTo(this.fieldInfo.getBall().getPosition());
				//System.out.println(smallerDistanceOutherTeam-smallerDistanceMyTeam);
			}return smallerDistanceMyTeam <= smallerDistanceOtherTeam || smallerDistanceOtherTeam-smallerDistanceMyTeam >= 10;
		
		}else{
			for(PlayerPerception p: this.fieldInfo.getTeamPlayers(EFieldSide.LEFT)) {
				if(p.getPosition().distanceTo(this.fieldInfo.getBall().getPosition()) < smallerDistanceMyTeam)
					smallerDistanceOtherTeam = p.getPosition().distanceTo(this.fieldInfo.getBall().getPosition());
			}
			for(PlayerPerception p: this.fieldInfo.getTeamPlayers(EFieldSide.RIGHT)) {
				if(p.getPosition().distanceTo(this.fieldInfo.getBall().getPosition()) < smallerDistanceMyTeam)
					smallerDistanceMyTeam = p.getPosition().distanceTo(this.fieldInfo.getBall().getPosition());
				//System.out.println(smallerDistanceOutherTeam-smallerDistanceMyTeam);
			}return smallerDistanceMyTeam <= smallerDistanceOtherTeam || smallerDistanceOtherTeam-smallerDistanceMyTeam >= 10;
		}
	}
	
	private boolean closerToTheBall() {
		
		ArrayList<PlayerPerception> players = new ArrayList<PlayerPerception>();
		int distanceIndex=0;
		double auxA, auxB;
		
		players.addAll(fieldInfo.getAllPlayers());
		Vector2D ballPosition = fieldInfo.getBall().getPosition();
		auxA = pointDistance(players.get(0).getPosition(), ballPosition);
		
		for(int i=0;i<players.size();i++){
			auxB = pointDistance(players.get(i).getPosition(), ballPosition);
			if(auxA > auxB){
				distanceIndex = i;
			}
		}
		
		distanceIndex++;
		//System.out.println(">>>>>>>>>>"+distanceIndex);
		return selfInfo.getUniformNumber() == distanceIndex;  
	}
	
	private int closerToThePlayer() {
		
		ArrayList<PlayerPerception> players = new ArrayList<PlayerPerception>();
		int distanceIndex=0;
		double auxA, auxB;
		
		players.addAll(fieldInfo.getTeamPlayers(selfInfo.getSide()));
		Vector2D playerPosition = selfInfo.getPosition();
		auxA = pointDistance(players.get(0).getPosition(), playerPosition);
		
		for(int i=0;i<players.size();i++){
			auxB = pointDistance(players.get(i).getPosition(), playerPosition);
			if(auxA > auxB){
				distanceIndex = i;
			}
		}
		
		//distanceIndex++;
		//System.out.println(">>>>>>>>>>"+distanceIndex);
		return distanceIndex;  
	}
	
	/////// Estado ATTACKING ///////	
	
	private void stateAttacking() {
		if (!teamHasBall()) {
			state = State.BLOCKING;
			//System.out.println(selfInfo.getUniformNumber()+">>>>>>"+"Attacking>>>>>>>>>>>>>>>Blocking");
			//System.out.println(selfInfo.getUniformNumber()+">>>>>>>>>>>>>>>>>>>>>>>>>>>>blooooooooocccckkkkkkiiiiiiiinnnnnnnnggggg");
			return;
		}		

		Vector2D ballPosition = fieldInfo.getBall().getPosition();
		Vector2D playerPosition = selfInfo.getPosition();
		int closerToTheGoal = closerToTheGoal();			
						
		if (arrivedAt(ballPosition)) {
			//if(selfInfo.getUniformNumber() == closerToTheGoal ){
			
			if(!isAlignedTo(goalPosition)){
				commander.doTurnToPoint(goalPosition);
			}else{
				if(playerPosition.getX() >= xAttacking){
					//System.out.println("BICUDO SEM SENTIDO 1");
					commander.doKick(100, 0);
					state = State.RETURN_TO_HOME;
					//System.out.println(selfInfo.getUniformNumber()+">>>>>>"+"Attacking>>>>>>>>>>>>>>>Return to home");
					return;
				}
			//}
			else{
				//System.out.println("BICUDO SEM SENTIDO 2");
				double distance = 100*(selfInfo.getPosition().distanceTo(fieldInfo.getTeamPlayer(selfInfo.getSide(), 5).getPosition())/40);
				commander.doKickToPoint(distance, new Vector2D(fieldInfo.getTeamPlayer(selfInfo.getSide(), 5).getPosition()));
				//state = State.RETURN_TO_HOME;
				////System.out.println(selfInfo.getUniformNumber()+">>>>>>"+ "Attacking>>>>>>>>>>>>>>>Return to home");
				//return;
			}
			}		
				

		}
		else{
			if(!isAlignedTo(ballPosition)){
				commander.doTurnToPointBlocking(ballPosition);
			}			
			commander.doDashBlocking(100);
		}			
	}
	
	//for debugging
	public void _printf(String format, Object...objects) {
		String teamPlayer = "";
		if (selfInfo != null) {
			teamPlayer += "[" + selfInfo.getTeam() + "/" + selfInfo.getUniformNumber() + "] ";
		}
		System.out.printf(teamPlayer + format + "%n", objects);
	}

}