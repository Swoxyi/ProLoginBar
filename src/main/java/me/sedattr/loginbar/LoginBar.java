package me.sedattr.loginbar;

import me.sedattr.loginbar.actionbar.ActionBar1_8;
import me.sedattr.loginbar.actionbar.ActionBar1_10;
import me.sedattr.loginbar.commands.SendCommand;
import me.sedattr.loginbar.commands.ServerCommand;
import me.sedattr.loginbar.helpers.BossBar;
import me.sedattr.loginbar.helpers.Server;
import me.sedattr.loginbar.title.Title1_8;
import me.sedattr.loginbar.title.Title1_11;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class LoginBar extends JavaPlugin {
    private static final String API_URL = "https://heisensetups.com.tr/app/api/v1/check.php?product=2&=xxx";
    public void loadVariables() {
        Variables.config = getConfig();
        Variables.bungeecord = Variables.config.getBoolean("bungeecord");
        if (Variables.bungeecord)
            Variables.server = new Server(this);

        String version = Bukkit.getVersion();

        Variables.title = version.contains("1.7") || version.contains("1.8") || version.contains("1.9") || version.contains("1.10") ? new Title1_8() : new Title1_11();
        Variables.actionBar = version.contains("1.7") || version.contains("1.8") || version.contains("1.9") ? new ActionBar1_8() : new ActionBar1_10();
        Variables.bossBar = version.contains("1.7") || version.contains("1.8") ? null : new BossBar();
    }

    public void onEnable() {
        licenseCheck();
        saveDefaultConfig();
        if (!Bukkit.getPluginManager().isPluginEnabled("AuthMe")) {
            Bukkit.getConsoleSender().sendMessage("§8[§bDeluxeBazaar§8] §cI can't find AuthMe! Plugin is disabling...");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        loadVariables();
        new Events(this);
        if (Variables.bungeecord) {
            Bukkit.getConsoleSender().sendMessage("§8[§bProLoginBar§8] §eEnabled Bungeecord support.");
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

            PluginCommand send = getCommand("send");
            if (send != null)
                send.setExecutor(new SendCommand());

            PluginCommand server = getCommand("server");
            if (server != null)
                server.setExecutor(new ServerCommand());
        }

        Bukkit.getConsoleSender().sendMessage("§8[§bProLoginBar§8] §aPlugin is successfully enabled! §fv" + getDescription().getVersion());
    }

    private void licenseCheck() {
        try {
            String localIPAddress = getLocalIPAddress();
            if (localIPAddress == null) {
                System.out.println("Local IP address not found. Plugin is shutting down...");
                return;
            }

            JSONObject response = sendGET(API_URL + localIPAddress);

            Boolean status = (Boolean) response.get("status");
            if (status != null && status) {
                Bukkit.getConsoleSender().sendMessage("License found!");
                Bukkit.getConsoleSender().sendMessage("Plugin is starting...");
            } else {
                Bukkit.getConsoleSender().sendMessage("License not found. Plugin is shutting down...");
                Bukkit.getConsoleSender().sendMessage("License IP: " + localIPAddress);
                Bukkit.shutdown();
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private String getLocalIPAddress() throws SocketException {
        Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = (NetworkInterface) interfaces.nextElement();
            if (iface.isLoopback() || !iface.isUp()) {
                continue;
            }
            Enumeration addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = (InetAddress) addresses.nextElement();
                if (addr.isLinkLocalAddress() || addr.isLoopbackAddress() || addr.isMulticastAddress()) {
                    continue;
                }
                return addr.getHostAddress();
            }
        }
        return null;
    }

    private JSONObject sendGET(String url) throws IOException, ParseException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(response.toString());
        } else {
            throw new IOException("Response code: " + responseCode);
        }
    }

    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§8[§bProLoginBar§8] §cPlugin is successfully disabled!");
    }
}
