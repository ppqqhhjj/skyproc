package skyproc;

/**
 * Types of GRUP records that skyproc can currently import.
 * @see SPImporter
 * @author Justin Swanson
 */
public enum GRUP_TYPE {

    /**
     * Game Settings
     */
    GMST,
    /**
     * Keywords
     */
    KYWD,
    /**
     * Texture Sets
     */
    TXST,
    /**
     *
     */
    GLOB,
    /**
     * Factions
     */
    FACT,
    /**
     *
     */
    HDPT,
    /**
     * Races
     */
    RACE,
    /**
     * Magic Effects
     */
    MGEF,
    /**
     *
     */
    ENCH,
    /**
     * Spells
     */
    SPEL,
    /**
     * Armors
     */
    ARMO,
    /**
     * Ingredients
     */
    INGR,
    /**
     *
     */
    MISC,
    /**
     * Alchemy
     */
    ALCH,
    /**
     * Craftable Object
     */
    COBJ,
    /**
     * Weapons
     */
    WEAP,
    /**
     * Ammo
     */
    AMMO,
    /**
     * Non-Player Characters (Actors)
     */
    NPC_,
    /**
     * Leveled Lists
     */
    LVLN,
    /**
     *
     */
    LVLI,
    /**
     * Image Spaces
     */
    QUST,
    /**
     *
     */
    IMGS,
    /**
     * Form Lists
     */
    FLST,
    /**
     * Perks
     */
    PERK,
    /**
     *
     */
    AVIF,
    /**
     * Armatures
     */
    ARMA,
    /**
     *
     */
    ECZN,
    /**
     *
     */
    OTFT;

    static boolean unfinished (GRUP_TYPE g) {
	switch (g) {
	    default:
		return false;
	}
    }

    static GRUP_TYPE toRecord (Enum e) {
	return GRUP_TYPE.valueOf(e.toString());
    }
}