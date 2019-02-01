package example.MATHENEWTEAM;


import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;

public class Team extends AbstractTeam {

	public Team(String suffix) {
		super("P" + suffix, 7, false);
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		//double targetX, targetY;
		
		if(ag==0){			
			//Zagueiro p = new Zagueiro(commander, -25, 20);
			PlayerGoalkeeper p = new PlayerGoalkeeper(commander);
			p.start();
		}else if(ag==1){			
			Zagueiro p = new Zagueiro(commander, -25, -20);
			p.start();
		}else if(ag==2){			
			//Atacante a = new Atacante(commander, 10, 0);
			Zagueiro p = new Zagueiro(commander, -25, 20);
			p.start();
		}else if(ag==3){
			Zagueiro p = new Zagueiro(commander, -30, 0);
			p.start();
		}else if(ag==4){
			Atacante a = new Atacante(commander, -10, 0);
			a.start();
		}else if(ag==5){
			Lateral l = new Lateral(commander, -5, 28, 11);
			l.start();
		}else if(ag==6){
			Lateral l = new Lateral(commander, -5, -28, -11);
			l.start();
		}
	}
}
