package net.mtrop.doom.tools.decohack;

/**
 * Common DeHackEd Patch interface.
 * @author Matthew Tropiano
 */
public interface DEHPatch
{
	public static final int AMMO_BULLETS = 0;
	public static final int AMMO_SHELLS  = 1;
	public static final int AMMO_CELLS   = 2;
	public static final int AMMO_ROCKETS = 3;

	public static final int WEAPON_FIST           = 0;
	public static final int WEAPON_PISTOL         = 1;
	public static final int WEAPON_SHOTGUN        = 2;
	public static final int WEAPON_CHAINGUN       = 3;
	public static final int WEAPON_ROCKETLAUNCHER = 4;
	public static final int WEAPON_PLASMA         = 5;
	public static final int WEAPON_BFG            = 6;
	public static final int WEAPON_CHAINSAW       = 7;
	public static final int WEAPON_SUPERSHOTGUN   = 8;
	
	/**
	 * @return the miscellaneous object.
	 */
	DEHMiscellany getMiscellany();

	/**
	 * @return the amount of ammo definitions.
	 */
	int getAmmoCount();

	/**
	 * Gets an ammo descriptor.
	 * @param index the index.
	 * @return the corresponding ammo object.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 * @see #getAmmoCount() 
	 */
	DEHAmmo getAmmo(int index);
	
	/**
	 * @return the amount of strings.
	 */
	int getStringCount();
	
	/**
	 * Gets a string.
	 * @param index the index.
	 * @return the corresponding string.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 * @see #getStringCount() 
	 */
	String getString(int index);
	
	/**
	 * @return the string offset for sound names, or null if not supported.
	 */
	Integer getSoundStringIndex();
	
	/**
	 * Gets the matching sound index for a sound name.
	 * @param name the sound name.
	 * @return the sound index, or null if no match.
	 */
	Integer getSoundIndex(String name);
	
	/**
	 * @return the string offset for sprite names, or null if not supported.
	 */
	Integer getSpriteStringIndex();
	
	/**
	 * Gets the matching sprite index for a sprite name.
	 * @param name the sprite name.
	 * @return the sprite index, or null if no match.
	 */
	Integer getSpriteIndex(String name);
	
	/**
	 * @return the amount of sound entries.
	 */
	int getSoundCount();

	/**
	 * Gets a sound by a sound index (0-based).
	 * @param index the index.
	 * @return the corresponding sound.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 * @see #getSoundCount() 
	 */
	DEHSound getSound(int index);

	/**
	 * @return the amount of things in the patch.
	 */
	int getThingCount();

	/**
	 * Gets a thing by a thing index.
	 * @param index the index.
	 * @return the corresponding thing.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 * @see #getThingCount()
	 */
	DEHThing getThing(int index);
	
	/**
	 * @return the amount of things in the patch.
	 */
	int getWeaponCount();

	/**
	 * Gets a weapon by a weapon index.
	 * @param index the index.
	 * @return the corresponding weapon.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 * @see #getWeaponCount() 
	 */
	DEHWeapon getWeapon(int index);
	
	/**
	 * @return the amount of states in the patch.
	 */
	int getStateCount();

	/**
	 * Gets a state by a state index.
	 * @param index the index.
	 * @return the corresponding state.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 * @see #getStateCount() 
	 */
	DEHState getState(int index);

	/**
	 * Gets the action pointer index using a state index.
	 * @param stateIndex the state index.
	 * @return the corresponding pointer index.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 * @see #getStateCount() 
	 */
	Integer getStateActionPointerIndex(int stateIndex);

	/**
	 * Gets the amount of action pointers.
	 * This number may be managed differently according to implementation.
	 * @return the amount of action pointers.
	 */
	int getActionPointerCount();
	
	/**
	 * Gets an action pointer.
	 * This may be managed differently according to implementation.
	 * @param index the pointer index.
	 * @return the corresponding action pointer, or null if not pointer.
	 */
	DEHActionPointer getActionPointer(int index);
	
}
