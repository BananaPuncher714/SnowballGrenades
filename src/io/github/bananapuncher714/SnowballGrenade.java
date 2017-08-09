package io.github.bananapuncher714;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.util.Vector;

public class SnowballGrenade extends JavaPlugin implements Listener {
	private boolean snowball, egg;
	private HashMap< UUID, Integer > bounces = new HashMap< UUID, Integer >();
	private boolean explode = true;
	private int bounceAmount = 4;
	private boolean damageTerrain = false;
	private float power = ( float ) 6.9;
	private double slow = .65;
	private boolean dispensers = true;
	private boolean snowmen = false;
	private boolean players = true;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		loadSnowballConfig();
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		bounces.clear();
	}
	
	public void loadSnowballConfig() {
		try {
			FileConfiguration config = getConfig();
			snowball = config.getBoolean( "enable-snowballs" );
			egg = config.getBoolean( "enable-eggs" );
			explode = config.getBoolean( "explosions" );
			bounceAmount = config.getInt( "bounce-amount" );
			damageTerrain = config.getBoolean( "damage-terrain" );
			power = ( float ) config.getDouble( "explosion-power" );
			slow = config.getDouble( "bounce-reduction" );
			dispensers = config.getBoolean( "dispensers" );
			snowmen = config.getBoolean( "snowmen" );
			players = config.getBoolean( "players" );
		} catch ( Exception e ) {
			getLogger().info( "There has been a problem with the configuration. Assuming default values." );
			snowball = true;
			egg = false;
			explode = true;
			bounceAmount = 4;
			damageTerrain = false;
			power = ( float ) 6.9;
			slow = .65;
			dispensers = true;
			snowmen = false;
			players = true;
		}
	}
	
	@EventHandler
	public void onProjectileLaunchEvent( ProjectileLaunchEvent e ) {
		Projectile proj = e.getEntity();
		if ( ( proj instanceof Snowball && snowball ) || ( proj instanceof Egg && egg ) ) {
			if ( !bounces.containsKey( proj.getUniqueId() ) ) {
				if ( ( proj.getShooter() instanceof BlockProjectileSource && dispensers ) || ( proj.getShooter() instanceof Player && players ) || ( proj.getShooter() instanceof Snowman && snowmen ) ) {
					bounces.put( proj.getUniqueId() , bounceAmount );
				}
			}
		}
	}
	
	@EventHandler
	public void onProjectileHitEvent( ProjectileHitEvent e ) {
		Entity ent = e.getEntity();
		if ( ent instanceof Snowball && bounces.containsKey( ent.getUniqueId() ) ) {
            Location loc = ent.getLocation().getBlock().getLocation();
            if ( bounces.get( ent.getUniqueId() ) <= 0 ) {
            	if ( explode ) loc.getWorld().createExplosion( loc.getX(), loc.getY(), loc.getZ(), ( float ) power, false, damageTerrain );
            	bounces.remove( ent.getUniqueId() );
            	return;
            }
            Vector vec = ent.getVelocity();
            if ( loc.clone().add( 1, 0, 0 ).getBlock().getType() == Material.AIR && loc.clone().add( -1, 0, 0 ).getBlock().getType() == Material.AIR ) {
            	vec.setX( - vec.getX() );
            } 
            if ( loc.clone().add( 0, 1, 0 ).getBlock().getType() == Material.AIR && loc.clone().add( 0, -1, 0 ).getBlock().getType() == Material.AIR ) {
            	vec.setY( - vec.getY() );
            } 
            if ( loc.clone().add( 0, 0, 1 ).getBlock().getType() == Material.AIR && loc.clone().add( 0, 0, -1 ).getBlock().getType() == Material.AIR ) {
            	vec.setZ( - vec.getZ() );
            }
			Snowball snowball = ent.getWorld().spawn( ent.getLocation(), Snowball.class );
            vec.setX( vec.getX() * ( - slow ) );
            vec.setY( vec.getY() * ( - slow ) );
            vec.setZ( vec.getZ() * ( - slow ) );
			snowball.setVelocity( vec );
            bounces.put( snowball.getUniqueId(), bounces.get( ent.getUniqueId() ) - 1 );
            bounces.remove( ent.getUniqueId() );
		}
	}
}
