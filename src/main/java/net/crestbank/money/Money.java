package net.crestbank.money;

import lombok.Getter;
import net.crestbank.money.commands.CommandHandler;
import net.crestbank.money.database.StorageManager;
import net.crestbank.money.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * 🏦 CrestBank: Industrial-Grade Economy & Financial Infrastructure
 * ----------------------------------------------------------------
 * This is the central core of the CrestBank ecosystem. It manages all 
 * administrative systems, forensic auditing, and institutional wealth tracking.
 * 
 * Built with safety-first principles, CrestBank ensures atomic transaction 
 * integrity and high-performance data processing for modern Minecraft servers.
 * 
 * @author CrestMC Team
 * @version 1.0.0
 */
public class Money extends JavaPlugin {

    @Getter private static Money instance;
    
    // Core Services
    @Getter private ConfigHandler configHandler;
    @Getter private StorageManager storageManager;
    @Getter private BankManager bankManager;
    @Getter private InterestManager interestManager;
    @Getter private NoteManager noteManager;
    @Getter private GuiManager guiManager;
    @Getter private InterestBoostManager boostManager;
    @Getter private AlertManager alertManager;
    @Getter private ConfirmationManager confirmationManager;
    @Getter private LogManager logManager;
    @Getter private UpdateChecker updateChecker;
    
    // Global States
    @Getter private boolean lockdown = false;
    @Getter private boolean maintenance = false;

    private static final Logger log = Logger.getLogger("Minecraft");

    /**
     * Plugin Initialization Protocol
     * Handles service boot-loading and system readiness checks.
     */
    @Override
    public void onEnable() {
        instance = this;

        printBanner();
        log.info(String.format("┃ [%s] Industrial Financial Core initializing...", getDescription().getName()));

        // Service Boot-loading Phase
        initializeServices();

        // Data Consistency Phase
        storageManager.setupStorage();

        // Command & Event Registration
        registerProtocols();

        // Background Task Sequencing
        scheduleBackgroundTasks();

        // External Hooking
        initializeIntegrations();

        log.info(String.format("┃ [%s] Institutional node ONLINE. v%s", getDescription().getName(), getDescription().getVersion()));
    }

    private void initializeServices() {
        log.info("┃ Initializing core banking services...");
        this.configHandler = new ConfigHandler(this);
        this.storageManager = new StorageManager(this);
        this.bankManager = new BankManager(this);
        this.interestManager = new InterestManager(this);
        this.noteManager = new NoteManager(this);
        this.guiManager = new GuiManager(this);
        this.boostManager = new InterestBoostManager(this);
        this.alertManager = new AlertManager(this);
        this.confirmationManager = new ConfirmationManager(this);
        this.logManager = new LogManager(this);
        this.updateChecker = new UpdateChecker(this);
        
        updateChecker.checkForUpdates();
        new org.bstats.bukkit.Metrics(this, 30066);
    }

    private void registerProtocols() {
        log.info("┃ Registering network protocols and gateways...");
        CommandHandler handler = new CommandHandler(this);
        getCommand("bank").setExecutor(handler);
        getCommand("bank").setTabCompleter(handler);

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SignListener(this), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
    }

    private void scheduleBackgroundTasks() {
        log.info("┃ Synchronizing background financial tasks...");
        interestManager.startInterestTask();
        logManager.startPruningTask();
    }

    private void initializeIntegrations() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            log.info("┃ PlaceholderAPI located. Establishing PAPI-Hook...");
            new PapiHook(this).register();
        }
    }

    private void printBanner() {
        Bukkit.getConsoleSender().sendMessage("§8§m------------------------------------------");
        Bukkit.getConsoleSender().sendMessage("§b§l  C R E S T B A N K §7- Professional Ecosystem");
        Bukkit.getConsoleSender().sendMessage("§f  Institution: §eCrestMC");
        Bukkit.getConsoleSender().sendMessage("§f  Security: §aIndustrial-Grade §8(v" + getDescription().getVersion() + ")");
        Bukkit.getConsoleSender().sendMessage("§8§m------------------------------------------");
    }

    /**
     * Emergency System Controls
     */
    public void setLockdown(boolean lockdown) { 
        this.lockdown = lockdown; 
        log.warning(String.format("[%s] GLOBAL LOCKDOWN STATE CHANGED: %s", getDescription().getName(), lockdown ? "HALTED" : "OPERATIONAL"));
    }

    public void setMaintenance(boolean maintenance) { 
        this.maintenance = maintenance; 
        log.info(String.format("[%s] MAINTENANCE MODE STATE CHANGED: %s", getDescription().getName(), maintenance ? "ENABLED" : "DISABLED"));
    }

    /**
     * Plugin Termination Protocol
     */
    @Override
    public void onDisable() {
        log.info(String.format("┃ [%s] Safely suppressing institutional nodes...", getDescription().getName()));
        
        if (storageManager != null) storageManager.shutdown();
        if (interestManager != null) interestManager.stopInterestTask();
        
        log.info(String.format("┃ [%s] All transaction gateways CLOSED.", getDescription().getName()));
    }
}
