package BlockyPortals.blockynights;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Sign;

public class PortalCreate implements Listener {
	
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/db";
	static final String USER = "user";
	static final String PASS = "pass";
	
	  private Connection connect = null;
	  private Statement statement = null;
	  private PreparedStatement preparedStatement = null;
	  private ResultSet resultSet = null;
	  private Connection conn = null;
	  private Statement stmt = null;
	
	private Map<Location, String> protect = new HashMap<Location, String>();	
	private Map<Location, String> signs = new HashMap<Location, String>();	
	
	public main plugin = main.getPlugin();
	
	public PortalCreate(main instance) {
	plugin = instance;
	}


	@EventHandler
	private void onPlayerInteract(PlayerInteractEvent event) {
	    Player p = event.getPlayer();
	    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
	        Block clicked = event.getClickedBlock();
	        if (clicked.getType() == Material.STONE_BUTTON || clicked.getType() == Material.WOOD_BUTTON) {
	        	Material mat = p.getLocation().subtract(0, 1, 0).getBlock().getType();
	            Location block = p.getLocation().add(0, 2, 0).getBlock().getLocation();
				String b = block.getWorld().getName() + " , " +  block.getX() + " , " + block.getY() + " , " + block.getZ()+ " , " + block.getPitch() + " , " + block.getYaw();
	            if(mat == Material.WOOL && isProtected(block)) {
					 if (block.getBlock().getRelative(BlockFace.NORTH).getType() == Material.WALL_SIGN) { 
							org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getBlock().getRelative(BlockFace.NORTH).getState();
							String msg = sign.getLine(2).substring(2);
							telePlayer(msg,p,b);
						 } 
					 if (block.getBlock().getRelative(BlockFace.EAST).getType() == Material.WALL_SIGN) { 
							org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getBlock().getRelative(BlockFace.EAST).getState();
							String msg = sign.getLine(2).substring(2);
							telePlayer(msg,p,b);
						 }  
					 if (block.getBlock().getRelative(BlockFace.WEST).getType() == Material.WALL_SIGN) {  
							org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getBlock().getRelative(BlockFace.WEST).getState();
							String msg = sign.getLine(2).substring(2);
							telePlayer(msg,p,b);
						 }  
					 if (block.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.WALL_SIGN) {  
							org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getBlock().getRelative(BlockFace.SOUTH).getState();
							String msg = sign.getLine(2).substring(2);
							telePlayer(msg,p,b);
					 }  
	            }
	         }
	     }
	}

	// Create portal //
	@EventHandler
	private void onSignChanged(SignChangeEvent event) {
		Player p = event.getPlayer();
		String uuid = p.getUniqueId().toString();
		Sign s = (Sign) event.getBlock().getState().getData();
		Block attached = event.getBlock().getRelative(s.getAttachedFace());
		if (attached.getType().toString() == "WOOL") {
			if (attached.getLocation().subtract(0, 1, 0).getBlock().getType().toString() == "STONE_BUTTON" || attached.getLocation().subtract(0, 1, 0).getBlock().getType().toString() == "WOOD_BUTTON") {
				if (!isProtected(attached.getLocation())) {
				String[] lines = event.getLines();
					if( lines[0].equalsIgnoreCase("blockyportal")) {
					if (isNumeric(lines[2])) {
						String freq = lines[2];
						if (!isPrivate(freq) || isOwner(uuid,freq) || p.hasPermission("bp.admin")) {
							Location locatched = attached.getLocation();
							String locatachedstring = locatched.getWorld().getName() + " , " +  locatched.getX() + " , " + locatched.getY() + " , " + locatched.getZ()+ " , " + locatched.getPitch() + " , " + locatched.getYaw();
							Location locblock = event.getBlock().getLocation();
							String locblockstring = locblock.getWorld().getName() + " , " +  locblock.getX() + " , " + locblock.getY() + " , " + locblock.getZ()+ " , " + locblock.getPitch() + " , " + locblock.getYaw();
							String line3 = "§2[Public]";
							int priv = 0;
							if (lines[3].equalsIgnoreCase("private")) { line3 = "§4[Private]"; priv = 1; }
								int teleid = sqlFindCreateID(lines[2]);
								writeSign(event.getBlock().getLocation(),p.getDisplayName(),lines[1],lines[2],line3); 
					            sqlCreate(p.getDisplayName(),locatachedstring,teleid,lines[2],priv,uuid,locblockstring,1,p,line3,event.getBlock().getLocation());
					            sqlTeleCheck(lines[2]);
								addToMap(attached.getLocation(),uuid,event.getBlock().getLocation());
								updateOnPrivateChange(freq,line3);

							} else { p.sendMessage("§3Sorry, this frequency is PRIVATE. Choose another"); }
						} else { p.sendMessage("§3Frequency MUST be numeric!"); }
					}
				}
				else { event.setCancelled(true); p.sendMessage("Block is already a portal"); }
			}
		}
	}
		
	// PROTECTION //
	@EventHandler
	private void onBlockPistonRetract(BlockPistonRetractEvent e) {
		BlockFace p = e.getDirection();
		 Location l = e.getBlock().getRelative(p, -2).getLocation();
		 if (isProtected(l)) {
			 e.setCancelled(true);
		 }
	 }
	
	@EventHandler
	private void onBlockPistonRetract(BlockPistonExtendEvent e) {
		BlockFace p = e.getDirection();
		 Location l = e.getBlock().getRelative(p, -1).getLocation();
		 if (isProtected(l)) {
			 e.setCancelled(true);
		 }
	 }
	 
	 @EventHandler
	 private void onPlace(BlockPlaceEvent e) {
		 if (e.getBlockPlaced().getType() == Material.WALL_SIGN) {
			 if (e.getBlockAgainst().getType() == Material.WOOL) {
				if (isProtected(e.getBlockAgainst().getLocation())) {
					e.setCancelled(true);
				}
			}
		 }
	 }
	 @EventHandler
	 private void onBlockBreakEvent(BlockBreakEvent e) {
		 Player p = (Player) e.getPlayer();
		 String uuid = e.getPlayer().getUniqueId().toString();
		 Block block = e.getBlock();
		 if (isProtected(block.getLocation())) {
			 e.setCancelled(true);
		 }
		if (block.getType().toString() == "WALL_SIGN") {
			Sign s = (Sign) e.getBlock().getState().getData();
			Block attached = e.getBlock().getRelative(s.getAttachedFace());
			if (isProtected(attached.getLocation())) {
				if (isSignProtected(block.getLocation(),uuid) || p.hasPermission("bp.admin")) {
				org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getState();
				String msg = sign.getLine(2);
				removePortalProtection(attached.getLocation(),e.getBlock().getLocation());
				sqlFreqGetCount(msg.substring(2));
				sqlTeledelete(msg.substring(2));
				} else { e.getPlayer().sendMessage("§3Sorry, we cant just having you go around breaking other peoples stuff!"); e.setCancelled(true); }
			}
		}
	  }
	 
	 private boolean isProtected(Location loc) {
		 if (protect.get(loc) != null) {
		 return true;
		 }
		 return false;
	}
	 private boolean isSignProtected(Location loc,String uuid) {
		 if (signs.get(loc).equals(uuid)) {
		 return true;
		 }
		 return false;
	}
	
	 private void writeSign(Location loc,String p,String line1,String line2,String line3) {
		final Location l = loc;
		final String player = p;
		final String lin1 = line1;
		final String lin2 = line2;
		final String lin3 = line3;
        main.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(main.getPlugin(), new Runnable() {
            public void run() {
                if (l.getBlock().getType() == Material.WALL_SIGN) {
                    org.bukkit.block.Sign sign = (org.bukkit.block.Sign) l.getBlock().getState();
                    sign.setLine(0, "§b" + player);
                    sign.setLine(1, lin1);
                    sign.setLine(2, "§a" + lin2);
                    sign.setLine(3, lin3);
                    sign.update(true);
                }
            }
        }, 2L);
	}
	
	private void updateSigns(Location loc,String status) {
	final Location l = loc;
	final String priv = status;
    main.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(main.getPlugin(), new Runnable() {
        public void run() {
            if (l.getBlock().getType() == Material.WALL_SIGN) {
                org.bukkit.block.Sign sign = (org.bukkit.block.Sign) l.getBlock().getState();
                sign.setLine(3, priv);
                sign.update(true);
            }
        }
    }, 2L);
	}
	
	private void removePortalProtection(Location l,Location sign) {
	 protect.remove(l);
		String block = sign.getWorld().getName() + " , " +  sign.getX() + " , " + sign.getY() + " , " + sign.getZ()+ " , " + sign.getPitch() + " , " + sign.getYaw();
		sqlRemove(block);
	}
	private void addToMap(Location l, String s,Location sign) {
			protect.put(l, s);
			signs.put(sign, s);
	}
	private static boolean isNumeric(String str)
	{
	  return str.matches("-?\\d+(\\.\\d+)?");
	}
	
	private Location stringToLocation(String key){
        String[] split = key.split(" , ");
        if(split.length == 6){
        Location loc = new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
        return loc;
        }else{
            return null;
        }
   
   
    }
	
	// SQL //
	
	private void telePlayer(String freq, Player p,String b){
		try{
			      Class.forName("com.mysql.jdbc.Driver");
			      conn = DriverManager.getConnection(DB_URL,USER,PASS);
			      stmt = conn.createStatement();
			      String sql = "SELECT Tele_id FROM tele WHERE Freq='"+freq+"'";
			      ResultSet rs = stmt.executeQuery(sql);
			      if (rs.next()) {
			    	  int tele_id = rs.getInt("Tele_id");
			    	  stmt.close();
			    	  stmt = conn.createStatement();
				      sql = "SELECT Location,Tele_id FROM portals WHERE Freq='"+freq+"' AND Tele_id='"+tele_id+"' ";
				      rs = stmt.executeQuery(sql);
				      if (rs.next()) {
				    	 String loc = rs.getString("Location");
				    	 if (!b.equals(loc)) {
				    		 p.teleport(stringToLocation(loc).subtract(0,2,0).add(0.5,0,0.5));
				    	 }
				    	 else { getNewID(freq,p,b); }
				      }
			      }
			      stmt.close();conn.close();
		   }catch(SQLException se){se.printStackTrace();}catch(Exception e){e.printStackTrace();}
	   		finally{try{if(stmt!=null)stmt.close();}catch(SQLException se2){}try{if(conn!=null)conn.close();}catch(SQLException se){se.printStackTrace();}}
	 }
	
	private void getNewID(String freq, Player p,String b) {
		try{
			      Class.forName("com.mysql.jdbc.Driver");
			      conn = DriverManager.getConnection(DB_URL,USER,PASS);
			      stmt = conn.createStatement();
			      String sql = "SELECT COUNT(*) as count FROM portals WHERE Freq='"+freq+"';";
			      ResultSet rs = stmt.executeQuery(sql);
			      if (rs.next()) {
			    	  int id_portal = rs.getInt("count");
			    	  stmt.close();
			    	  stmt = conn.createStatement();
			    	  sql = "SELECT Tele_id FROM tele WHERE Freq='"+freq+"';";
			    	  rs = stmt.executeQuery(sql);
			    	  if (rs.next()){
			    		  int id_tele = rs.getInt("Tele_id");
				    	  int id = id_tele +1;
				    	  if (id > id_portal) { id = 1; }
				    	  stmt = conn.createStatement();
					      sql = "UPDATE tele SET tele_id='"+id+"' WHERE Freq='"+freq+"'";
					      stmt.executeUpdate(sql);
					      telePlayer(freq,p,b);
				    	  }
			      }
			      stmt.close();conn.close();
		   }catch(SQLException se){se.printStackTrace();}catch(Exception e){e.printStackTrace();}
	   		finally{try{if(stmt!=null)stmt.close();}catch(SQLException se2){}try{if(conn!=null)conn.close();}catch(SQLException se){se.printStackTrace();}}
	 }
	
	private void sqlTeleCheck(String freq) {
		   try{
			      Class.forName("com.mysql.jdbc.Driver");
			      conn = DriverManager.getConnection(DB_URL,USER,PASS);
			      stmt = conn.createStatement();
			      String sql;
			      sql = "SELECT COUNT(*) as count from portals WHERE Freq='"+freq+"';";
			      ResultSet rs = stmt.executeQuery(sql);
			      if (rs.next()) {
			      int check = rs.getInt("count");
			      	if (check == 1) { 
			      		stmt.close();
					    stmt = conn.createStatement();
					    sql = "INSERT INTO tele (Freq,Tele_id) VALUES('"+freq+"','1');";
					    stmt.executeUpdate(sql);
			      	}
			      }
			    rs.close();stmt.close();conn.close(); 
		   }catch(SQLException se){se.printStackTrace();}catch(Exception e){e.printStackTrace();}
	   		finally{try{if(stmt!=null)stmt.close();}catch(SQLException se2){}try{if(conn!=null)conn.close();}catch(SQLException se){se.printStackTrace();}}
	 }
	
	private void sqlTeledelete(String freq) {
		   try{
			      Class.forName("com.mysql.jdbc.Driver");
			      conn = DriverManager.getConnection(DB_URL,USER,PASS);
			      stmt = conn.createStatement();
			      String sql;
			      sql = "SELECT COUNT(*) as count from portals WHERE Freq='"+freq+"';";
			      ResultSet rs = stmt.executeQuery(sql);
			      if (rs.next()) {
			      int check = rs.getInt("count");
			      	if (check == 0) { 
			      		stmt.close();
					    stmt = conn.createStatement();
					    sql = "DELETE FROM tele WHERE Freq='"+freq+"';";
					    stmt.executeUpdate(sql);
			      	}
			      }
			    rs.close();stmt.close();conn.close(); 
		   }catch(SQLException se){se.printStackTrace();}catch(Exception e){e.printStackTrace();}
	   		finally{try{if(stmt!=null)stmt.close();}catch(SQLException se2){}try{if(conn!=null)conn.close();}catch(SQLException se){se.printStackTrace();}}
	 }
	
	public void updateOnEnable() {
		try {
		Class.forName("com.mysql.jdbc.Driver");
	      connect = DriverManager.getConnection(DB_URL,USER,PASS);
	      statement = connect.createStatement();
	      resultSet = statement
	      .executeQuery("SELECT * from portals");
	      while (resultSet.next()) {
	    	  String loc = resultSet.getString("Location");
	    	  String uuid = resultSet.getString("uuid");
	    	  String sloc = resultSet.getString("sign_loc");
	    	  addToMap(stringToLocation(loc),uuid,stringToLocation(sloc));
	      }
	      statement.close();connect.close();
 		}catch(SQLException se){se.printStackTrace();}catch(Exception e){e.printStackTrace();}
		finally{try{if(statement!=null)statement.close();}catch(SQLException se2){}try{if(connect!=null)connect.close();}catch(SQLException se){se.printStackTrace();}}
	}
	
	private void sqlRemove(String loc) {
		   try{
			      Class.forName("com.mysql.jdbc.Driver");
			      connect = DriverManager.getConnection(DB_URL,USER,PASS);
			      statement = connect.createStatement();
			      preparedStatement = connect
			     .prepareStatement("delete from portals where sign_loc= ? ; ");
			      preparedStatement.setString(1, loc);
			      preparedStatement.executeUpdate();
		   		}catch(SQLException se){se.printStackTrace();}catch(Exception e){e.printStackTrace();}
	   		finally{try{if(statement!=null)statement.close();}catch(SQLException se2){}try{if(connect!=null)connect.close();}catch(SQLException se){se.printStackTrace();}}
	 }

	
	private void updateOnPrivateChange(String freq,String too) {
		int to;
		if (too.equals("§4[Private]")) { to = 1; }
		else { to = 0; }
		try{
			      Class.forName("com.mysql.jdbc.Driver");
			      conn = DriverManager.getConnection(DB_URL,USER,PASS);
			      stmt = conn.createStatement();
			      String sql;
			      sql = "SELECT * from portals WHERE Freq='"+freq+"' AND Private<>'"+to+"';";
			      ResultSet rs = stmt.executeQuery(sql);
			      while (rs.next()) {
			    	String location = rs.getString("sign_loc");
					updateSigns(stringToLocation(location),too);
			      }
			      sql = "UPDATE portals SET Private='"+to+"' WHERE Freq ='"+freq+"';";
			      stmt.executeUpdate(sql);
			      rs.close();stmt.close();conn.close();
		   }catch(SQLException se){se.printStackTrace();}catch(Exception e){e.printStackTrace();}
	   		finally{try{if(stmt!=null)stmt.close();}catch(SQLException se2){}try{if(conn!=null)conn.close();}catch(SQLException se){se.printStackTrace();}}
	 }
	
	private boolean isPrivate(String i) {
		   try{
			      Class.forName("com.mysql.jdbc.Driver");
			      conn = DriverManager.getConnection(DB_URL,USER,PASS);
			      stmt = conn.createStatement();
			      String sql;
			      sql = "SELECT * from portals WHERE Freq='"+i+"' AND Private='1';";
			      ResultSet rs = stmt.executeQuery(sql);
			      if (rs.next()) { rs.close();stmt.close();conn.close(); return true; } else {rs.close();stmt.close();conn.close(); return false; }
		   }catch(SQLException se){se.printStackTrace();}catch(Exception e){e.printStackTrace();}
	   		finally{try{if(stmt!=null)stmt.close();}catch(SQLException se2){}try{if(conn!=null)conn.close();}catch(SQLException se){se.printStackTrace();}}return true;
	 }
	
	private boolean isOwner(String uuid,String freq) {
		   try{
			      Class.forName("com.mysql.jdbc.Driver");
			      conn = DriverManager.getConnection(DB_URL,USER,PASS);
			      stmt = conn.createStatement();
			      String sql;
			      sql = "SELECT * from portals WHERE Freq='"+freq+"' AND uuid='"+uuid+"';";
			      ResultSet rs = stmt.executeQuery(sql);
			      if (rs.next()) { rs.close();stmt.close();conn.close(); return true; } else {rs.close();stmt.close();conn.close(); return false; }
		   }catch(SQLException se){se.printStackTrace();}catch(Exception e){e.printStackTrace();}
	   		finally{try{if(stmt!=null)stmt.close();}catch(SQLException se2){}try{if(conn!=null)conn.close();}catch(SQLException se){se.printStackTrace();}}return true;
	 }


	private void sqlCreate(String owner,String loc,int tele,String freq,int priv,String uuid,String sign_loc,int active,Player p,String too,Location loco) {
		   try{
			      Class.forName("com.mysql.jdbc.Driver");
			      conn = DriverManager.getConnection(DB_URL,USER,PASS);
			      stmt = conn.createStatement();
			      String sql;
			      sql = "INSERT INTO portals(Owner,Location,Tele_id,Freq,Private,uuid,sign_loc,active)"
			      + "VALUES ('"+owner+"','"+loc+"','"+tele+"','"+freq+"','"+priv+"','"+uuid+"','"+sign_loc+"','"+active+"')";
			      stmt.executeUpdate(sql);
			      ;stmt.close();conn.close();
		   }catch(SQLException se){se.printStackTrace();}catch(Exception e){e.printStackTrace();}
	   		finally{try{if(stmt!=null)stmt.close();}catch(SQLException se2){}try{if(conn!=null)conn.close();}catch(SQLException se){se.printStackTrace();}}}
	
	private int sqlFindCreateID(String freq) {
		   try{
			      Class.forName("com.mysql.jdbc.Driver");
			      conn = DriverManager.getConnection(DB_URL,USER,PASS);
			      stmt = conn.createStatement();
			      String sql;
			      sql = "SELECT COUNT(*) as count FROM portals WHERE Freq='"+freq+"'";
			      ResultSet rs = stmt.executeQuery(sql);
			      int freq_id;
			      if (rs.next()) { freq_id = rs.getInt("count") +1; rs.close();stmt.close();conn.close(); return freq_id;
			      } else {
			    	  freq_id = rs.getInt("count") +1; rs.close();stmt.close();conn.close(); return freq_id; 
			    	  }
		   }catch(SQLException se){se.printStackTrace();}catch(Exception e){e.printStackTrace();}
	   		finally{try{if(stmt!=null)stmt.close();}catch(SQLException se2){}try{if(conn!=null)conn.close();}catch(SQLException se){se.printStackTrace();}}return 1;
	 }
	
	private void sqlFreqGetCount(String fre) {
		try {
		Class.forName("com.mysql.jdbc.Driver");
	      connect = DriverManager.getConnection(DB_URL,USER,PASS);
	      stmt = connect.createStatement();
	      resultSet = stmt
	      .executeQuery("SELECT * from portals WHERE Freq='"+fre+"' GROUP BY Freq;");
	      while (resultSet.next()) {
	    	  int freq = resultSet.getInt("Freq");
	    	  stmt.close();
	    	  sqlFreqAmount(freq);
	      }
	      stmt.close();connect.close();
 		}catch(SQLException se){se.printStackTrace();}catch(Exception e){e.printStackTrace();}
		finally{try{if(statement!=null)statement.close();}catch(SQLException se2){}try{if(connect!=null)connect.close();}catch(SQLException se){se.printStackTrace();}}
	}
	
	private void sqlFreqAmount(int freq) {
		try {
		Class.forName("com.mysql.jdbc.Driver");
	      connect = DriverManager.getConnection(DB_URL,USER,PASS);
	      statement = connect.createStatement();
	      resultSet = statement
	      .executeQuery("SELECT COUNT(*) AS count FROM portals WHERE Freq='"+freq+"';");
	      while (resultSet.next()) {
	    	  int count = resultSet.getInt("count");
	    	  sqlTeleSorter(freq,count);
	      }
	      statement.close();connect.close();
 		}catch(SQLException se){se.printStackTrace();}catch(Exception e){e.printStackTrace();}
		finally{try{if(statement!=null)statement.close();}catch(SQLException se2){}try{if(connect!=null)connect.close();}catch(SQLException se){se.printStackTrace();}}
	}
	
	private void sqlTeleSorter(int freq,int amount) {
		try{
			      Class.forName("com.mysql.jdbc.Driver");
			      conn = DriverManager.getConnection(DB_URL,USER,PASS);
			      stmt = conn.createStatement();
			      String sql;
			      sql = "SELECT Tele_id,ID from portals WHERE Freq='"+freq+"' ORDER BY ID ASC;";
			      ResultSet rs = stmt.executeQuery(sql);
			      int i = 0;
			      int id = 0;
			      while (i < amount) {
			    	  rs.next();
			    	  i++;
			    	  stmt = conn.createStatement();
					  id = rs.getInt("ID");
				      sql = "UPDATE portals SET Tele_id='"+i+"' WHERE ID ='"+id+"';";
				      stmt.executeUpdate(sql);
			      }
			      stmt.close();conn.close();
		   }catch(SQLException se){se.printStackTrace();}catch(Exception e){e.printStackTrace();}
	   		finally{try{if(stmt!=null)stmt.close();}catch(SQLException se2){}try{if(conn!=null)conn.close();}catch(SQLException se){se.printStackTrace();}}
	 }
	
}

