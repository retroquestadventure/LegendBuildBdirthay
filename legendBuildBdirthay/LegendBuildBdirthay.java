package legendBuildBdirthay;

/* Ich verwende hier:
 * https://github.com/discord-jda/JDA/releases
 * 
 * Das ist die Java Discord API
 */

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

////////////////////////////////////////////////////////////////

import javax.annotation.Nonnull;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class LegendBuildBdirthay {
	
	/* Nuutzt Java Discord API -> Hier wirnd die Klasse von
	ListenerAdapter abgeleitet */
	public static class DiscordBirthdayBot extends ListenerAdapter {
		//Datenbankaddresse
	    private static final String DB_URL = "jdbc:sqlite:birthdays.db";
	    //Discord Kanal ID - Die ID von dem Kanal wo der Bot genutzt werden soll.
	    private static final String CHANNEL_ID = "<ChannelID>";
	    /* Discord Bot Token Anwedung unter
	     * https://discord.com/developers/applications
	     * regestriert werden.
	     */
	    private static final String BOT_TOKEN = "<Discord Bot Token>U";

	    public static void main(String[] args) throws Exception {
	    	DiscordBirthdayBot discordBirthdayBot = new DiscordBirthdayBot();
	        JDA jda = JDABuilder.createDefault(BOT_TOKEN, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
	                .addEventListeners(discordBirthdayBot)
	                .build();
	        //Initialisiere DB
	        initDatabase();
	        //Funktion zum Überprüfen des Geburtstags wird aufgerufen
	        startBirthdayCheck(jda);
	    }

	    public static void initDatabase() {
	    	//Versuche eine Verbindung zur Datenbank
	        try (Connection conn = DriverManager.getConnection(DB_URL);
	        	// Verbbindung ok - Baue SQL Satement und fphre es aus
	             Statement stmt = conn.createStatement()) {
	            stmt.execute("CREATE TABLE IF NOT EXISTS birthdays (user_id TEXT PRIMARY KEY, date TEXT, year INTEGER)");
	        } catch (SQLException e) {
	        	//Falls ein Fehler aufgetreten ist wird eine Fehlermeldung angezeigt
	            e.printStackTrace();
	        }
	    }
	    @Override
	    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
	    	// Befehl der vom User ausgelößt wurde auswerten
	    	/* Beispiel: 
	    	 *  /gb eintrag 31.05.1980
	    	 */
	        String[] args = event.getMessage().getContentRaw().split(" ");
	        if (args.length >= 3 && args[0].equalsIgnoreCase("/gb") && args[1].equalsIgnoreCase("eintrag")) {
	            String userId = event.getAuthor().getId();
	            String date = args[2];
	            Integer year = (args.length == 4) ? Integer.parseInt(args[3]) : null;
	            saveBirthday(userId, date, year);
	            // Dem User wird eine Nachricht angezeigt nach dem der Befehl verwendet wurde.
	            event.getChannel().sendMessage("Geburtstag gespeichert für " + event.getAuthor().getAsMention()).queue();
	        }
	    }
	    
	    // Speichert den Geubrtstag der eingegeben wurde in der DB ( falls er schon vorhanden war, wird er überschrieben )
	    private void saveBirthday(String userId, String date, Integer year) {
	        try (Connection conn = DriverManager.getConnection(DB_URL);
	            PreparedStatement pstmt = conn.prepareStatement("REPLACE INTO birthdays (user_id, date, year) VALUES (?, ?, ?)");) {
		            pstmt.setString(1, userId);
		            pstmt.setString(2, date);
		            
		            // Der Eintrag des Geburtsjahr ist Optional
		            if (year != null) {
		                pstmt.setInt(3, year);
		            } else {
		                pstmt.setNull(3, Types.INTEGER);
		            }
		            pstmt.executeUpdate();
		        } catch (SQLException e) {
		            e.printStackTrace();
		        }
	    }
	    
	    //Funktion zum Überprüfen des Geburtstags
	    private static void startBirthdayCheck(JDA jda) {
	        Timer timer = new Timer(true);
	        timer.scheduleAtFixedRate(new TimerTask() {
	            @Override
	            public void run() {
	                checkBirthdays(jda);
	            }
	        }, 0, 86400000);
	        // 86400000
	    }

	    public static void checkBirthdays(JDA jda) {
	        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM"));
	        try (Connection conn = DriverManager.getConnection(DB_URL);
        		// SQL Abfrage ob der User mit der user_id heute ( "today" ) Geburtstag hat.
	            PreparedStatement pstmt = conn.prepareStatement("SELECT user_id, year FROM birthdays WHERE date = ?")) {
	            pstmt.setString(1, today);
	            ResultSet rs = pstmt.executeQuery();
	            /* In diesem Discord Kanal mit der ID "CHANNEL_ID" wird die Ausgabe erfolgen falls die Abfrage ein Positives
	            Ergebniss liefert und der Kanal exestiert */
	            TextChannel channel = jda.getTextChannelById(CHANNEL_ID);
	            while (rs.next() && channel != null) {
	                String userId = rs.getString("user_id");
	                Integer year = rs.getInt("year");
	                // Die Ausgabe wie alt man ist erfolgt nur falls das Geburtsjahr angegeben wurde.
	                year = 1;
	                String ageMsg = (year != 0) ? " (" + (LocalDate.now().getYear() - year) + " Jahre alt)" : "";
	                channel.sendMessage("🎉 Herzlichen Glückwunsch zum Geburtstag <@" + userId + ">! 🎂" + ageMsg).queue();
	                System.out.println("\"🎉 Herzlichen Glückwunsch zum Geburtstag <@\" + userId + \">! 🎂\" + ageMsg).queue()");
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}

}
