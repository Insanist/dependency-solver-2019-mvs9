package depsolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

class Package {
  private String name;
  private String version;
  private Integer size;
  private List<List<String>> depends = new ArrayList<>();
  private List<String> conflicts = new ArrayList<>();

  public String getName() { return name; }
  public String getVersion() { return version; }
  public Integer getSize() { return size; }
  public List<List<String>> getDepends() { return depends; }
  public List<String> getConflicts() { return conflicts; }
  public void setName(String name) { this.name = name; }
  public void setVersion(String version) { this.version = version; }
  public void setSize(Integer size) { this.size = size; }
  public void setDepends(List<List<String>> depends) { this.depends = depends; }
  public void setConflicts(List<String> conflicts) { this.conflicts = conflicts; }
}

public class Main {
	
  public static List<Package> repo;
  public static List<String> constraints;
  public static int cheapest = 1000000;
  public static List<String> finalins; //final instructions
  public static List<String> visited = new ArrayList<>();
  
  public static void main(String[] args) throws IOException {
	
	String repoPath = null;
	String initialPath = null;
	String constraintsPath = null;
	
	if(args.length==0) {
		repoPath = "tests/example-0/repository.json";
		initialPath = "tests/example-0/initial.json";
		constraintsPath = "tests/example-0/constraints.json";
	}
	else {
		repoPath = args[0];
		initialPath = args[1];
		constraintsPath = args[2];
	}
	  
    TypeReference<List<Package>> repoType = new TypeReference<List<Package>>() {};  
    repo = JSON.parseObject(readFile(repoPath), repoType);    
    TypeReference<List<String>> strListType = new TypeReference<List<String>>() {};
    List<String> initial = JSON.parseObject(readFile(initialPath), strListType);
    
    constraints = JSON.parseObject(readFile(constraintsPath), strListType);
    
    /******************************************
     HACK SOLUTION FOR SEEN 0 LOL
    Stack installList = new Stack();
    List<String> constraintNeeded =  new ArrayList<>();
    List<String> constraintNot =  new ArrayList<>();
    int cheapest = 0;
	String name = null;
	String version = null;
    boolean sameName = false;
    List<String> packageName = new ArrayList<>(); 
    for(Package p : repo) {
    	packageName.add(p.getName());
    	cheapest = p.getSize();
    }
    for(String p : packageName) {
    	if(p == packageName.get(0)) { sameName = true; }
    }
    if(sameName) {
    	for(Package p : repo) {
    		if(p.getSize() < cheapest) {
    			cheapest = p.getSize();
    			version = p.getVersion();
    			name = p.getName();
    		}
    	}
    }
    //System.out.println(printPackage(name, version, true, true));

    ******************************************/
    
    // start with initial repo
    List<String> initList = new ArrayList<>();
	for(Package p : repo) {	
		if(initList.isEmpty()) {
			initList.add("");
		}
    	for(String i : initial) {
	    	String[] pckg = i.split("=");
	    	if(pckg.length>1) { 
	    		if(pckg[0].equals(p.getName()) && pckg[1].equals(p.getVersion())) { initList.add(i); }
	    	}
	    	else if (pckg.length==1){
	    		if(pckg[0].equals(p.getVersion())) {initList.add(i); }
	    	}
	    }
	}

	
	
	List<String> solution = new ArrayList<>();
	iterate(initList, solution);
	String build = printPackage(finalins);
	System.out.println(build);
  }
  
  // initList will either be [] or ["A=2.2",["B=2.1"] etc, but only work with one
  public static void iterate(List<String> initList, List<String> solution) {
	  //System.out.println("--------------------------------------------------------------------------------##");
	  //System.out.println();
	  
	  if(initList.size()==1) {
		  if(initList.get(0).equals("")) { initList.remove(0); }
	  }
	  
	  boolean seen = false;
	  int counter = 0;
	  for(String l : initList) {
		  if (visited.contains(l) && !visited.isEmpty()) {
			  counter++;
		  }
	  }
	  if(counter==initList.size() && !visited.isEmpty()) {
		  seen = true;
	  }
	  
	  /**System.out.println("CYCLED?? "+cycle(initList));
	  System.out.println("IS VALID??? "+isValid(initList));
	  System.out.println("IS FINAL??? " + last(initList));
	  System.out.println("LIST SIZE BEFORE: " + initList.size());**/

	  if( seen==false && isValid(initList)) {
		  //System.out.println("LIST SIZE AFTER 1 : " + initList.size());
		  
		  for(String i: initList) {
			  visited.add(i);
		  }
		  
		// ONLY IF FINAL
		  if(last(initList) == true) {
			  int solutionSize = calculateSize(solution);
			  //System.out.println("SOL SIZE : "+solutionSize);
			  if(solutionSize < cheapest) {
				  cheapest = solutionSize;
				  finalins = solution; // the instructionns
			  }
			 
		  }
		  
		  //ALWAYS GO HERE
		  else {
			  for(Package p : repo) {
				  boolean installed = false;
				  String pname = p.getName();
				  String pversion = p.getVersion();
				  String pbuild = pname+"="+pversion;
				  
				  for(String i : initList) {
					  if(pbuild.equals(i)) { installed = true; }
				  }
				  
				  //System.out.println("is it true???? " + installed);

				  List<String> newlist = initList;
				  List<String> newins = solution;
				  
				  //System.out.println("INSTALLED?!?!?!" + installed);
				  
				  if(installed==false) {
					  //System.out.println("actioned?!?!?!" + actioned('-', solution, p));
					  
					  if( actioned('-', solution, p) == false ) {
						  
						  List<String> temp = new ArrayList<>(initList);
						  String name = p.getName();
						  String version = p.getVersion();
						  String build = name+"="+version;
						  temp.add(build);
						  newlist = temp;
						  
						  String name2 = p.getName();
						  String version2 = p.getVersion();
						  String build2 = '+' + name2 + "=" + version2;
						  List<String> temp2 = new ArrayList<>(solution);
						  temp2.add(build2);
						  newins = temp2;
					  }
				  }
				  else { //if installed true, if actioned is false or solution is less than constraints, and there is still to uninstall
					  //System.out.println("NOT HITTING HERE");
					  boolean x = (actioned('+', solution, p) == false || solution.size() < constraints.size()) && toMinus(p);
					  //System.out.println("--- ACTIONED " + x);
					  if( (actioned('+', solution, p) == false || solution.size() < constraints.size()) && toMinus(p) ){
						  List<String> temp = new ArrayList<>(initList);
						   String build = p.getName() + "=" + p.getVersion();
						   for(String l : initList) {
							   if(l.equals(build)) { temp.remove(l); } 
						   }	   
						  newlist = temp;
						  
						  String name2 = p.getName();
						  String version2 = p.getVersion();
						  String build2 = '-' + name2 + "=" + version2;
						  List<String> temp2 = new ArrayList<>(solution);
						  temp2.add(build2);
						  newins = temp2;
					  }
				  }
				  
				  iterate(newlist,newins);
			  }
		  }
		  for(String i : initList) {
			  visited.remove(i);
		  }  
	  }
  }
  
  //to do with deps and conflicts ///RETURNIN FALSE WHEN EG A=2.01
  public static boolean isValid(List<String> initList) {
	  
	  if( initList.isEmpty() || initList.contains("")) { return true; }
	  
	  // mostRecent = the enddddddd
	  
	  String latest = initList.get(initList.size()-1);	 
	  String [] ls = latest.split("=");
	  List<List<String>> currentDeps = new ArrayList<List<String>>();
	  for(Package p : repo) {
		  if(ls[0].equals(p.getName()) && ls[1].equals(p.getVersion())){
			  currentDeps = p.getDepends();
		  }
	  }
	  
	  
	  //System.out.println("CHECKDEPS FALSE OR TRUE??? "+checkDeps(initList, currentDeps));
	  if(checkDeps(initList, currentDeps)==false) { return false; }	//
	  //System.out.println("RETURNING CHECK CONF?? "+ checkConf(initList));
	  return checkConf(initList);
  }
  
  public static boolean toMinus(Package p) {
	String name = p.getName();
	String version = p.getVersion();
	String build = "";
	
	if(version.equals("")) { build = name;}
	else { build = name+"="+build; }
	
	boolean bool = false;
	for(String c : constraints) {
		if(c.equals('+'+build)) { bool = false;; }
		else if (c.equals('-'+build)) { bool = true;; } 
	}
	
	return true;
  }
  
  public static List<String> instruct(boolean b,List<String> solution, Package p){
	  String name = p.getName();
	  String version = p.getVersion();
	  char c;
	  if(b) { c = '+'; }
	  else { c = '-'; }
	  String build = c + name + "=" + version;
	  List<String> temp = new ArrayList<>(solution);
	  temp.add(build);
	  
	  return temp;
  }
  
  public static boolean actioned(char a, List<String> solution, Package p) {
	  String name = p.getName();
	  String version = p.getVersion();
	 
	  String exp = ""+a;	  
	  if(version.equals("")) { exp += name; }
	  else { exp +="="+version; }
	  
	  for(String s : solution) {
		  if(s.equals(exp)) { return true;}
		  else { return false; }
	  }
	  return false;
  }
  
  public static int calculateSize(List<String> solution) {
	  int size = 0;
	  
	  for(String s : solution) {
		  if(s.charAt(0)=='+') {
			  String [] sols = s.substring(1).split("=");
			  String name = null;
			  String version = null;
			  if(sols.length>1) { name = sols[0]; version = sols[1]; }
			  else { name = sols[0];}
			  
			  if(version.equals("")) {
				   //find the size in the repo
				  for(Package p: repo) {
					  if(name.equals(p.getName())) {
						  if(p != null) { size += p.getSize(); }
					  }
				  }
			  }
			  else {
				  for(Package p : repo) {
					  if(name.equals(p.getName()) && version.equals(p.getVersion())){
						  if(p !=null) { size+=p.getSize(); }
					  }
				  }
			  }
		  }
		  else {
			  size += 1000000;
		  }
	  }
	  return size;
  }
  
  public static boolean last(List<String> list) {
	  for(String c : constraints) {
		  boolean done = false;
		  if(c.charAt(0)=='+') { done = true; }  
		  
		  String [] cons = c.substring(1).split("="); //starts  with + or -
		  String name = "";
		  String version = "";
		  if(cons.length>1) { name = cons[0]; version = cons[1]; }
		  else { name = cons[0];}
          
          if(done) {
        	  boolean installed = false;
        	  for(String i : list) {
        		  String [] is = i.split("=");
        		  if(version.equals("") && is[0].equals(name)) {  installed = true; }
        		  if(!version.equals("") && is[0].equals(name) && is[1].equals(version)) {  installed = true; }
        	  }
        	  if(installed==false) {; return false;}
          }
          else {
        	  for(String i : list) {
        		  String [] is = i.split("=");
        		  if(version.equals("") && is[0].equals(name)) {  return false; }
        		  if(!version.equals("") && is[0].equals(name) && is[1].equals(version)) {   return false; }
        	  }
          }
	  }
	  return true;
  }
  
  public static boolean checkConf(List<String> initList) {
	  for(String i : initList) {
		 String [] is = i.split("=");
		 String name = is[0];
		 String version = is[1];
		 List<String> conflicts = new ArrayList<>();
		 for(Package p : repo) {
			 if(name.equals(p.getName()) && version.equals(p.getVersion())) {
				 conflicts = p.getConflicts();
				 for(String c : conflicts) {
					 for(String iss : initList) {
						 if(!i.equals(iss)) { 
							 checkRequirements(c, p.getName(), p.getVersion());
						 }
					 }
				 }
			 }
		 }
	  }
	  return true;
  }
  
  public static boolean checkDeps(List<String> initList, List<List<String>> currentDeps) {
	  if(currentDeps.size()==0){ return true; }
	  if(currentDeps.size()>0) {
		  for(List<String> c : currentDeps) {
			  boolean check = false;
			  for(String d : c) {
				  for(String i : initList) {
					  String [] iss = i.split("=");
					  String name = iss[0];
					  String ver = iss[1];
					  if(checkRequirements(d, name, ver)) {
						  check = true;
					  }
				  }
			  }
			  if(check == false) { return false; }
		  }
	  }
	  return true;
  }
  
  public static boolean cycle1(String [] inits) {
	  boolean seen = false;
	  if(inits.length > 1) {
		  String name = inits[0];
		  String version = inits[1];
		  if(visited != null) {
		  	for(String v : visited) { 
				  String[] vs = v.split("=");
				  if(vs[0]==name && vs[1]==version) { seen = true; }	  
			  }
		  }
	  }
	  else if (inits.length == 1){
		  String name = inits[0];
		  if(visited != null) {
			  	for(String v : visited) { 
					  String[] vs = v.split("=");
					  if(vs[0]==name) { seen = true; }	  
				  }
			  }
	  }
	  return seen;
  }
 
  //returns  -1 if a<b, 0 if equal, 1 if a>b
  public static int compare(String a, String b) {
	  List<String> aStringList = Arrays.asList(a.split("\\."));
      List<String> bStringList = Arrays.asList(b.split("\\."));
      int[] aIntList = aStringList.stream().mapToInt(Integer::parseInt).toArray();
      int[] bIntList = bStringList.stream().mapToInt(Integer::parseInt).toArray();
	  
      int length = 0;
      if(aIntList.length>bIntList.length) { length = bIntList.length; }
      else length = aIntList.length;
      
      for(int i = 0; i<length; i++) {
          if(aIntList[i] < bIntList[i]) { return -1;}
          else if(aIntList[i] > bIntList[i]) { return 1; }
          else if(aIntList[i] == bIntList[i]) { continue; }
      }
     return 0;
  }
 

  public static boolean checkRequirements(String ver, String name, String version) {
	  if(ver.contains("=")) {
		  String [] v = ver.split("=");
		  if(v[0].equals(name) && compare(version, v[1])==0) { return true; }
		  else { return false; }
	  }
	  else if(ver.contains(">")) {
		  String [] v = ver.split(">");
		  if(v[0].equals(name) && compare(version, v[1])==-1) { return true;}
		  else { return false; }
	  }
	  else if(ver.contains(">=")) {
		  String [] v = ver.split(">=");
		  if(v[0].equals(name) && compare(version, v[1])>=0) { return true;}
		  else { return false; }
	  }
	  else if(ver.contains("<")) {
		  String [] v = ver.split("<");
		  if(v[0].equals(name) && compare(version, v[1])<=0) { return true;}
		  else { return false; }
	  }
	  else if(ver.contains("<=")) {
		  String [] v = ver.split("<=");
		  if(v[0].equals(name) && compare(version, v[1])<=0) { return true;}
		  else { return false; }
	  }
	  else {
		  if(ver.contentEquals(name)) { return true; }
		  else { return false; }
	  }
  }
  
  
  public static String printPackage(List<String> list){
	  String build = "[ ";	  
	  for(int i=0; i<list.size();i++) {
		  if(i==list.size()-1) {
			  build +="\"" + list.get(i) + "\"";
		  }
		  else {
			  build +="\"" + list.get(i) + "\", ";
		  }
	  }
	  build+=" ]";
	  return build;
  }
    
  static String readFile(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename));
    StringBuilder sb = new StringBuilder();
    br.lines().forEach(line -> sb.append(line));
    return sb.toString();
  }
}
