/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package skyproc.gui;

import skyproc.SkyProcTester;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import lev.Ln;
import lev.debug.LDebug;
import lev.gui.*;
import skyproc.*;

/**
 * SUM - SkyProc Unified Manager<br>
 * This is the main program that hooks together various SkyProc patchers and streamlines
 * their patching processing.
 * @author Justin Swanson
 */
public class SUMprogram implements SUM {

    ArrayList<String> exclude = new ArrayList<String>(2);
    ArrayList<PatcherLink> links = new ArrayList<PatcherLink>();
    Set<GRUP_TYPE> importRequests = new HashSet<GRUP_TYPE>();
    // GUI
    SPMainMenuPanel mmenu;
    HookMenu hookMenu;
    OptionsMenu optionsMenu;
    LScrollPane hookMenuScroll;
    LSaveFile saveFile = new SUMsave();
    Color blue = new Color(85, 50, 181);
    Font settingFont = new Font("Serif", Font.BOLD, 14);

    /**
     * Main function that starts the program and GUI.
     * @param args "-test" Opens up the SkyProc tester program instead of SUM
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	if (handleArgs(args)) {
	    SUMprogram sum = new SUMprogram();
	    sum.runProgram();
	}
    }

    static boolean handleArgs(String[] args) {
	ArrayList<String> argsList = new ArrayList<String>(Arrays.asList(args));
	if (argsList.contains("-test")) {
	    SkyProcTester.runTests();
	    return false;
	}
	return true;
    }

    void runProgram() throws InstantiationException, IllegalAccessException {
	exclude.add("SKYPROC UNIFIED MANAGER.JAR");
	exclude.add("SKYPROC.JAR");

	openDebug();
	saveFile.init();
	getHooks();
	openGUI();
	initLinkGUIs();

	for (PatcherLink link : links) {
	    // Compile all needed GRUPs
	    importRequests.addAll(Arrays.asList(link.hook.importRequests()));
	    // Block SUM patches from being imported)
	    SPGlobal.addModToSkip(link.hook.getListing());
	}

	// Import mods at start
	if (saveFile.getBool(SUMSettings.IMPORT_AT_START)) {
	    SUMGUI.runThread();
	}

    }

    void openDebug() {
	SPGlobal.createGlobalLog();
	SPGlobal.debugModMerge = false;
	SPGlobal.debugExportSummary = false;
	SPGlobal.debugBSAimport = false;
	SPGlobal.debugNIFimport = false;
	LDebug.timeElapsed = true;
	LDebug.timeStamp = true;
    }

    void openGUI() {
	mmenu = new SPMainMenuPanel();

	hookMenu = new HookMenu(mmenu, saveFile);
	mmenu.addMenu(hookMenu, false, saveFile, null);

	optionsMenu = new OptionsMenu(mmenu, saveFile);
	mmenu.addMenu(optionsMenu, false, saveFile, null);

	SUMGUI.open(this);
    }

    void initLinkGUIs() {
	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {

		SwingUtilities.invokeLater(new Runnable() {

		    @Override
		    public void run() {
			for (PatcherLink link : links) {
			    try {
				link.setup();
				hookMenu.hookMenu.add(link);
				hookMenu.revalidate();
			    } catch (Exception ex) {
				Logger.getLogger(SUMprogram.class.getName()).log(Level.SEVERE, null, ex);
			    }
			}
		    }
		});
	    }
	});
    }

    void getHooks() {
	ArrayList<File> jars = findJars(new File("../"));

	for (File jar : jars) {
	    try {
		ArrayList<Class> classes = Ln.loadClasses(jar, true);
		for (Class c : classes) {
		    Object tester = c.newInstance();
		    if (tester instanceof SUM) {
			links.add(new PatcherLink((SUM) c.newInstance(), jar));
		    }
		}
	    } catch (MalformedURLException ex) {
		SPGlobal.logException(ex);
	    } catch (FileNotFoundException ex) {
		SPGlobal.logException(ex);
	    } catch (IOException ex) {
		SPGlobal.logException(ex);
	    } catch (ClassNotFoundException ex) {
		SPGlobal.logException(ex);
	    } catch (InstantiationException ex) {
	    } catch (IllegalAccessException ex) {
	    }
	}

    }

    ArrayList<File> findJars(File dir) {
	ArrayList<File> files = Ln.generateFileList(dir, false);
	ArrayList<File> out = new ArrayList<File>();
	for (File f : files) {
	    if (f.getName().toUpperCase().endsWith(".JAR")
		    && !exclude.contains(f.getName().toUpperCase())) {
		out.add(f);
	    }
	}
	return out;
    }

    /**
     * Returns the modlisting used for the exported patch.
     * @return
     */
    @Override
    public ModListing getListing() {
	return new ModListing("SUM", false);
    }

    // Internal Classes
    class HookMenu extends SPSettingPanel {

	JPanel hookMenu;

	HookMenu(SPMainMenuPanel parent_, LSaveFile save) {
	    super("Patcher List", parent_, blue, save);
	    initialize();
	}

	@Override
	final public boolean initialize() {
	    if (super.initialize()) {

		save.setVisible(false);
		defaults.setVisible(false);

		hookMenu = new JPanel();
		hookMenu.setOpaque(false);
		hookMenu.setLayout(null);

		hookMenuScroll = new LScrollPane(hookMenu);
		hookMenuScroll.setSize(SUMGUI.middleDimensions.width,
			SUMGUI.middleDimensions.height - 100);
		hookMenuScroll.setLocation(0, 100);
		hookMenuScroll.setOpaque(false);
		hookMenuScroll.setBorder(null);
		hookMenuScroll.setVisible(true);
		Add(hookMenuScroll);

		return true;
	    }
	    return false;
	}
    }

    class OptionsMenu extends SPSettingPanel {

	LCheckBox importOnStartup;
	LCheckBox mergePatches;

	OptionsMenu(SPMainMenuPanel parent_, LSaveFile save) {
	    super("SUM Options", parent_, blue, save);
	}

	@Override
	final public boolean initialize() {
	    if (super.initialize()) {

		importOnStartup = new LCheckBox("Import On Startup", settingFont, SUMGUI.light);
		importOnStartup.addShadow();
		importOnStartup.tie(SUMSettings.IMPORT_AT_START, saveFile, SUMGUI.helpPanel, false);
		setPlacement(importOnStartup);
		AddSetting(importOnStartup);

		mergePatches = new LCheckBox("Merge Patches", settingFont, SUMGUI.light);
		mergePatches.addShadow();
		mergePatches.tie(SUMSettings.MERGE_PATCH, saveFile, SUMGUI.helpPanel, false);
		setPlacement(mergePatches);
		AddSetting(mergePatches);

		alignRight();

		return true;
	    }
	    return false;
	}
    }

    class PatcherLink extends LPanel {

	LImagePane logo;
	LLabel title;
	JCheckBox cbox;
	SUM hook;
	File path;
	JPanel menu;

	PatcherLink(final SUM hook, final File path) {
	    super();
	    this.hook = hook;
	    this.path = path;
	}

	final void setup() {
	    setupGUI();
	    setupHook();
	}

	final void setupGUI() {

	    Component using = null;

	    cbox = new JCheckBox();
	    cbox.setSize(cbox.getPreferredSize());
	    cbox.setOpaque(false);
	    cbox.setVisible(true);

	    int desiredWidth = SUMGUI.middleDimensions.width - 50;
	    int width = cbox.getWidth() + 10;

	    if (hook.hasLogo()) {
		try {
		    logo = new LImagePane(hook.getLogo());
		    if (logo.getWidth() + width > desiredWidth) {
			logo.setMaxSize(desiredWidth - width, 0);
		    }
		    using = logo;
		    add(logo);
		} catch (IOException ex) {
		    SPGlobal.logException(ex);
		    logo = null;
		}
	    }
	    if (logo == null) {
		title = new LLabel(hook.getName(), new Font("Serif", Font.BOLD, 14), hook.getHeaderColor());
		using = title;
		add(title);
	    }

	    width += using.getWidth();
	    cbox.setLocation(SUMGUI.middleDimensions.width / 2 - width / 2, using.getHeight() / 2 - cbox.getHeight() / 2);
	    add(cbox);
	    using.setLocation(cbox.getX() + cbox.getWidth() + 10, 0);
	    using.addMouseListener(new MouseListener() {

		@Override
		public void mouseClicked(MouseEvent arg0) {
		    mmenu.setVisible(false);
		    if (menu == null) {
			initMenu();
		    } else {
			menu.setVisible(true);
		    }
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
		}
	    });
	    setSize(SUMGUI.middleDimensions.width, using.getHeight());
	}

	final void setupHook() {
	    if (hook.hasSave()) {
		hook.getSave().init();
	    }
	}

	final void initMenu() {
	    menu = hook.getStandardMenu();
	    LButton close = new LButton("Close");
	    close.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
		    menu.setVisible(false);
		    mmenu.setVisible(true);
		}
	    });
	    menu.add(close);
	    SUMGUI.singleton.add(menu);
	}
    }

    class SUMsave extends LSaveFile {

	@Override
	protected void initSettings() {
	    Add(SUMSettings.IMPORT_AT_START, false, false);
	    Add(SUMSettings.MERGE_PATCH, false, false);
	}

	@Override
	protected void initHelp() {
	}
    }

    enum SUMSettings {

	MERGE_PATCH,
	IMPORT_AT_START;
    }

    // SUM methods
    /**
     *
     * @return
     */
    @Override
    public String getName() {
	return "SkyProc Unified Manager";
    }

    /**
     *
     * @return
     */
    @Override
    public GRUP_TYPE[] dangerousRecordReport() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @return
     */
    @Override
    public GRUP_TYPE[] importRequests() {
	GRUP_TYPE[] out = new GRUP_TYPE[0];
	out = importRequests.toArray(out);
	return out;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean importAtStart() {
	return false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasStandardMenu() {
	return true;
    }

    /**
     *
     * @return
     */
    @Override
    public SPMainMenuPanel getStandardMenu() {
	return mmenu;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasCustomMenu() {
	return false;
    }

    /**
     *
     * @return
     */
    @Override
    public JFrame openCustomMenu() {
	return null;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasLogo() {
	return false;
    }

    /**
     *
     * @return
     */
    @Override
    public URL getLogo() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasSave() {
	return true;
    }

    /**
     *
     * @return
     */
    @Override
    public LSaveFile getSave() {
	return saveFile;
    }

    /**
     *
     * @return
     */
    @Override
    public String getVersion() {
	return "1.0";
    }

    /**
     *
     * @return
     */
    @Override
    public Mod getExportPatch() {
	Mod patch = new Mod(getListing());
	patch.setFlag(Mod.Mod_Flags.STRING_TABLED, false);
	patch.setAuthor("Leviathan1753");
	return patch;
    }

    /**
     *
     * @return
     */
    @Override
    public Color getHeaderColor() {
	return Color.BLUE;
    }

    /**
     *
     * @throws Exception
     */
    @Override
    public void runChangesToPatch() throws Exception {
	if (saveFile.getBool(SUMSettings.MERGE_PATCH)) {
	    SPGlobal.setGlobalPatch(getExportPatch());
	    for (PatcherLink link : links) {
		SPGlobal.SUMpath = link.path.getParentFile().getAbsolutePath();
		link.hook.runChangesToPatch();
	    }
	} else {
	    for (int i = 0; i < links.size(); i++) {
		PatcherLink link = links.get(i);
		SPGlobal.SUMpath = link.path.getParentFile().getAbsolutePath() + "\\";
		SPGlobal.setGlobalPatch(link.hook.getExportPatch());
		link.hook.runChangesToPatch();
		if (i < links.size() - 1) { // Let normal routines export last patch
		    SPGlobal.getGlobalPatch().export();
		}
	    }
	}
    }
}
