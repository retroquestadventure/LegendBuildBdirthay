package legendBuildBdirthay;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import legendBuildBdirthay.LegendBuildBdirthay.DiscordBirthdayBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class main {
	public static void main(String[] args) throws Exception {
		
        DiscordBirthdayBot legendBuildBdirthay = new DiscordBirthdayBot();
        
        legendBuildBdirthay.initDatabase();
        									// Token hier einfügen
	    JDA jda = JDABuilder.createDefault("<Discord Bot Token>",
	    		   GatewayIntent.GUILD_MESSAGES,
	    		   GatewayIntent.MESSAGE_CONTENT)
	    		.enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new DiscordBirthdayBot())
                .build();
        
        jda.awaitReady(); // Warten, bis der Bot bereit ist
	    
	 // ScheduledExecutorService für regelmäßiges Abfragen
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Der Bot überprüft jeden Tag um Mitternacht ob wer Geburtstag hat
        //scheduler.scheduleAtFixedRate(() -> legendBuildBdirthay.checkBirthdays(jda), 0, 1, TimeUnit.DAYS);
        
        //macht es jede minute ( Als test )
        //scheduler.scheduleAtFixedRate(() -> legendBuildBdirthay.checkBirthdays(jda), 0, 1, TimeUnit.MINUTES);
        
        legendBuildBdirthay.checkBirthdays(jda);
    }

}
