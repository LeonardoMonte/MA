package example.MATHENEWTEAM;

import java.util.ArrayList;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.EMatchState;
import simple_soccer_lib.utils.Vector2D;

public class Atacante extends Thread{
private static final double ERROR_RADIUS = 1.0d;
	
	private enum State { ATTACKING, RETURN_TO_HOME, WAITING, KICKOFF, WAITING_ATTACK};

	private PlayerCommander commander;
	private State state;
	
	private PlayerPerception selfInfo;
	private FieldPerception  fieldInfo;
	private MatchPerception  matchInfo;
	
	private Vector2D homebase; //posi��o base do jogador
	
	private Vector2D goalPosition;
	
	private int[] numerosCamisa = {0,0,0,0,0,0,0};
	private boolean flag;
	
	//vari�ves para troca de lado
	private int[] xUniformNumber = {-52,-25,-10,-5};
	private int xAttacking = 36;
	private int xWaiting = 1;
	private int xAttackingToHome = -15;
	private int xAttackingKick = 26;
	
	public Atacante(PlayerCommander player, double x, double y) {
		commander = player;
		homebase = new Vector2D(x, y);
		flag = true;
		goalPosition = new Vector2D(52,0);
	}
	
	@Override
	public void run() {
		_printf("Waiting initial perceptions...");
		selfInfo  = commander.perceiveSelfBlocking();
		//System.out.println(">>>>>>>>>>>>>"+selfInfo.getUniformNumber());
		fieldInfo = commander.perceiveFieldBlocking();
		matchInfo = commander.perceiveMatchBlocking();
		
		state = State.RETURN_TO_HOME; //todos come�am neste estado
		
		if (selfInfo.getSide() == EFieldSide.RIGHT) { //ajusta a posi��o base de acordo com o lado do jogador (basta mudar o sinal do x)
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
			updatePerceptions();  //deixar aqui, no come�o do loop, para ler o resultado do 'move'
			
			if(flag) getCamisa();
			
			if (matchInfo.getState() == EMatchState.PLAY_ON) {				
				switch (state) {
				case ATTACKING:
					System.out.println("ATTACKING");
					stateAttacking();
					break;
				case RETURN_TO_HOME:
					System.out.println("RETURN TO HOME");
					stateReturnToHomeBase();
					break;
				case WAITING:
					System.out.println("WAITING");
					stateWaiting();
					break;
				case KICKOFF:
					System.out.println("KICKOFF");
					stateKickoff();
					break;
				case WAITING_ATTACK:
					System.out.println("WAITINGATTACK");
					stateWaitingAttack();
					break;
				default:
					_printf("Invalid state: %s", state);
					break;	
				}				
			}else if(matchInfo.getState() == EMatchState.KICK_OFF_LEFT || matchInfo.getState() == EMatchState.KICK_OFF_RIGHT){				
				//state = State.KICKOFF;
				stateKickoff();
				//return;
			}else if(matchInfo.getState() == EMatchState.CORNER_KICK_LEFT || matchInfo.getState() == EMatchState.CORNER_KICK_RIGHT){
				//System.out.println("<><><><><><><><><><><><><><><><><><><><><>");
				stateWaitingAttack();
			}else if(matchInfo.getState() == EMatchState.AFTER_GOAL_LEFT || matchInfo.getState() == EMatchState.AFTER_GOAL_RIGHT )
			{
				commander.doMoveBlocking(homebase.getX(), homebase.getY());
			}
			else if(matchInfo.getState() == EMatchState.GOAL_KICK_LEFT || matchInfo.getState() == EMatchState.GOAL_KICK_RIGHT )
			{
				commander.doMoveBlocking(homebase.getX(), homebase.getY());
			}
			else if(matchInfo.getState() == EMatchState.FREE_KICK_LEFT )
			{
				stateWaitingAttack();
			}
		}
			
	}
	
	private void updatePerceptions() {
		PlayerPerception newSelf = commander.perceiveSelf();
		FieldPerception newField = commander.perceiveField();
		MatchPerception newMatch = commander.perceiveMatch();
		
		// s� atualiza os atributos se tiver nova percep��o (sen�o, mant�m as percep��es antigas)
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
				System.out.println("Atacante ----- Hayao Miyazaki"+ p.getUniformNumber());
			}			
		}
	}
	
	private void swapSides(){		
		for(int i=0;i<xUniformNumber.length;i++){
			xUniformNumber[i] = xUniformNumber[i]*(-1);
		}
		homebase.setX(homebase.getX()*(-1));
		goalPosition.setX(goalPosition.getX()*(-1));
		xAttacking = xAttacking*(-1);
		xWaiting = xWaiting*(-1);
		xAttackingToHome = xAttackingToHome*(-1);
		xAttackingKick = xAttackingKick*(-1);
	}
	
	private boolean arrivedAtAt(Vector2D targetPosition, Vector2D agentPosition) {
		//Vector2D myPos = selfInfo.getPosition();
		return Vector2D.distance(agentPosition, targetPosition) <= ERROR_RADIUS+3;
	}
	
	/////// Estado waitingAttack ///////
	private void stateWaitingAttack(){
		//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Entrou!");
		Vector2D position;
		Vector2D ballPosition = fieldInfo.getBall().getPosition();
				
		if(arrivedAt(ballPosition)){
			commander.doKickToPoint(100.0d, goalPosition);
			commander.doTurnToPoint(goalPosition);
			state = State.ATTACKING;
			return;			
		}
		
		if(ballPosition.getY() > 0){
			position = new Vector2D(xAttacking,7);
		}else{
			position = new Vector2D(xAttacking,-7);
		}
		
		if(!arrivedAt(position)){
			if(!isAlignedTo(position)){
				commander.doTurnToPointBlocking(position);
			}else{
				commander.doDash(100);
			}			
		}else{
			commander.doTurnToPoint(ballPosition);
		}
	}
	
	
	
	/////// Estado kickoff ///////
	private void stateKickoff(){
		Vector2D ballPosition = fieldInfo.getBall().getPosition();
		Vector2D defenderPosition = fieldInfo.getTeamPlayer(selfInfo.getSide(), 6).getPosition();
				
		if (arrivedAt(ballPosition)) {
			//commander.doKickToPoint(80, fieldInfo.getTeamPlayer(selfInfo.getSide(), numerosCamisa[4]).getPosition());
			double distance = 100*(selfInfo.getPosition().distanceTo(defenderPosition)/40);
			commander.doKickToPointBlocking(distance, defenderPosition);
			Vector2D go = new Vector2D(15,0);			
			if (isAlignedTo(go)) {
				//_printf("ATK: Running to the ball...");
				commander.doDashBlocking(100.0d);
				state = State.ATTACKING;
			} else {
				//_printf("ATK: Turning...");
				turnTo(go);
			}
						
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
	
	/////// Estado WAITING ///////
	private void stateWaiting(){
		Vector2D ballPosition = fieldInfo.getBall().getPosition();
		Vector2D go = new Vector2D(15,0);
		if (isAlignedTo(go)) {
			//_printf("ATK: Running to the ball...");
			commander.doDashBlocking(75.0d);
			state = State.ATTACKING;
		} else {
			//_printf("ATK: Turning...");
			turnTo(go);
		}
		
		if(ballPosition.getX() >= 14){
			state = State.ATTACKING;
			return;
		}
		commander.doTurnToPoint(ballPosition);
	}

	////// Estado RETURN_TO_HOME_BASE ///////
	
	private void stateReturnToHomeBase() {
		Vector2D ballPosition = fieldInfo.getBall().getPosition();
		
		if (ballPosition.getX() > xWaiting) {
			state = State.ATTACKING;
			return;
		}
		
		if (! arrivedAt(homebase)) {			
			if (isAlignedTo(homebase)) {
				//_printf("RTHB: Running to the base...");
				commander.doDashBlocking(100.0d);			
			} else {
				//_printf("RTHB: Turning...");
				turnTo(homebase);
			}			
		}
		else{
			state = State.WAITING;
			return;	
		}
			
	}

	private boolean arrivedAt(Vector2D targetPosition) {
		Vector2D myPos = selfInfo.getPosition();
		return Vector2D.distance(myPos, targetPosition) <= ERROR_RADIUS;
	}

	private void turnTo(Vector2D targetPosition) {
		Vector2D myPos = selfInfo.getPosition();
		//System.out.println(" => Target = " + targetPosition + " -- Player = " + myPos);
		
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
	
	/////// Estado ATTACKING ///////	
	
	private void stateAttacking() {
		Vector2D ballPosition = fieldInfo.getBall().getPosition();
		Vector2D playerPosition = selfInfo.getPosition();
		
	//	if (ballPosition.getX() < xAttackingToHome) {
		//	state = State.RETURN_TO_HOME;
			//return;
		//}		
		
		Vector2D go = new Vector2D(15,0);
		
		if(ballPosition.getX() >= 14)
		{
			if (arrivedAt(ballPosition)) {
				
				if (isAlignedTo(goalPosition)) {
					commander.doTurnToPointBlocking(goalPosition);
					if(playerPosition.getX() >= xAttackingKick){
						commander.doKickBlocking(100, 0);
					}
					
					
				} else {
					
					turnTo(goalPosition);
				}

				
				commander.doKickBlocking(15, 0);
				
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
		else
		{
			if (isAlignedTo(go)) {
				//_printf("ATK: Running to the ball...");
				commander.doDashBlocking(70.0d);
				state = State.WAITING;
			} else {
				//_printf("ATK: Turning...");
				turnTo(go);
			}
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


