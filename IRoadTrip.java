// Source code is decompiled from a .class file using FernFlower decompiler.
import java.io.*;

//added
import java.util.*;

public class IRoadTrip {

    private Map<String,Map<String,Integer>> txtInfo;
    private Map<String,Map<String,Integer>> csvInfo;
    private Map<String, String> tsvInfo;
    private Map<String,String> fixedMap = new HashMap<>();
    private Map<String,Map<String,Integer>> finalInfo = new HashMap<>();
    
    public IRoadTrip(String[] args) {
        String txtFile;
        String csvFile;
        String tsvFile;
        fixedM();
        try {
            txtFile = args[0]; //txt file
            csvFile = args[1]; //csv file
            tsvFile= args[2]; //tsv file

            //try to read data in these files
            txtInfo = nearbyCountry(txtFile);
            csvInfo = correctDistance(csvFile);
            tsvInfo = countryToCode(tsvFile);
            
            //correct all country distance
            for(String key1 : txtInfo.keySet()){
                String fixedkey1 = key1;
                if(fixedMap.containsKey(key1)){
                    fixedkey1 = fixedMap.get(key1);
                }
                finalInfo.put(fixedkey1, new HashMap<>());
                //find country id
                if(tsvInfo.containsKey(key1)){
                    String tempCountryId1 = tsvInfo.get(key1);
                    
                    Map<String,Integer> temp = txtInfo.get(key1);
                    //use csv file to find correct distance,store the changed value in finalInfo
                    for(String key2 : temp.keySet()){
                        String fixedkey2 = key2;
                        if(fixedMap.containsKey(key2)){
                            fixedkey2 = fixedMap.get(key2);
                        }
                        //find country id
                        if(tsvInfo.containsKey(key2)){
                            String tempCountryId2 = tsvInfo.get(key2);
                            
                            if(csvInfo.containsKey(tempCountryId1)&&(csvInfo.get(tempCountryId1)).containsKey(tempCountryId2)){
                                //find correct distance and store distance in finalInfo
                                if((csvInfo.get(tempCountryId1)).containsKey(tempCountryId2)){
                                    int correctDistance = (csvInfo.get(tempCountryId1)).get(tempCountryId2);
                                    finalInfo.get(fixedkey1).put(fixedkey2,correctDistance);
                                }
                            }

                        }
                    }
                }
            }     
        } 
        catch (Exception e1) {System.err.println("Error1: " + e1.getMessage());System.exit(1);}

    }

    //get distance between two countries that shared borders
    public int getDistance(String country1, String country2) {
        int distance = -1;
        //check if it has another name
        if(fixedMap.containsKey(country1)){
            country1 = fixedMap.get(country1);
        }
        if(fixedMap.containsKey(country2)){
            country2 = fixedMap.get(country2);
        }
        if(finalInfo.containsKey(country1)&&finalInfo.get(country1).containsKey(country2)){
            distance = finalInfo.get(country1).get(country2);
        }         
        return distance;
    }

    public List<String> findPath(String country1, String country2) {
        String temp = "";
        fixedM();
        boolean flag = true;
        List<String> result = new ArrayList<>();
        Map<String, String> path = Dijkstra(country1, country2);
        
        if(fixedMap.containsKey(country1)){
            country1 = fixedMap.get(country1);
        }
        if(fixedMap.containsKey(country2)){
            country2 = fixedMap.get(country2);
        }
        String tempC = country2;
        //use an array to store the reversed order road
        List<String> reversedPath = new ArrayList<>();
        while(!tempC.equals(country1)){
            //reversedPath.add(tempC);
            if(path.get(tempC) == null){
                flag = false; break;
            }
            reversedPath.add(path.get(tempC));
            int tempDistance = getDistance(path.get(tempC),tempC);
            temp += "* "+path.get(tempC)+" --> "+tempC+" ("+tempDistance+" km.)\n";
            tempC = path.get(tempC);
        }
    
        String tempResult[] = temp.split("\n"); 
        for(int i=tempResult.length-1; i>=0; i--){
            result.add(tempResult[i]);
        }
        if(!flag){return new ArrayList<>();}
        return result;
    }

    //newly added
    //use Dijkstra to find the smallest path
    private class NodeDistance implements Comparable<NodeDistance>{
        String country;
        int distance;
        NodeDistance(String c, int d){
            country=c;
            distance=d;
        };
        
        public int compareTo(NodeDistance nc1)
        {   
            return Integer.compare(this.distance,nc1.distance);
        }
    }

    private Map<String, String> Dijkstra(String startC, String endC){
        fixedM();
        if(fixedMap.containsKey(startC)){
            startC = fixedMap.get(startC);
        }
        PriorityQueue<NodeDistance> distanceMinHeap = new PriorityQueue<>();
        distanceMinHeap.add(new NodeDistance(startC,0));

        Map<String, String> neigh = new HashMap<>();
        neigh.put(startC, "");

        HashSet<String> finalizedCountries = new HashSet<>();
       
        //store the current smallest distance from start country to current
        Map<String, Integer> smallestDistance = new HashMap<>();
        smallestDistance.put(startC, 0);

        String countryNow = startC;
        //until queue is empty or find the end country
        while(!distanceMinHeap.isEmpty()&&!endC.equals(countryNow)){
            //pop the first element, check if it is already in the finalized list
            NodeDistance countryTemp = distanceMinHeap.remove();
            while(!distanceMinHeap.isEmpty()&&finalizedCountries.contains(countryTemp.country)){
                countryTemp = distanceMinHeap.remove();
            }

            //if has no neighbors try to find another name of the country
            if(finalInfo.get(countryTemp.country) == null){
                if(fixedMap.containsKey(countryTemp.country)){
                    countryTemp = new NodeDistance(fixedMap.get(countryTemp.country), countryTemp.distance);
                }
            }
            
            int countryTempDis = countryTemp.distance;
            String countryTempNam = countryTemp.country;
            //mark it as finalized
            finalizedCountries.add(countryTemp.country);
           
            //check all of its neighbors
            for(String nearC : finalInfo.get(countryTempNam).keySet()){    
                int nearbyDistance = (finalInfo.get(countryTemp.country)).get(nearC);
                //if the country isn't be initialized
                //or if distance is smaller than the previous one
                if(fixedMap.containsKey(nearC)){nearC = fixedMap.get(nearC);}
                if(!smallestDistance.containsKey(nearC) ||
                    smallestDistance.get(nearC)>(nearbyDistance+countryTempDis)){
                    neigh.put(nearC,countryTempNam);
                    smallestDistance.put(nearC, nearbyDistance+countryTempDis);
                    distanceMinHeap.add(new NodeDistance(nearC, nearbyDistance+countryTempDis));
                }
                if(fixedMap.containsKey(nearC)){nearC = fixedMap.get(nearC);}
                
                countryNow = nearC;               
            }
        }
        return neigh;
    }

    //get countries nearby
    public Map<String,Map<String,Integer>> nearbyCountry(String txtFile){
        Map<String,Map<String,Integer>> linesInfo = new HashMap<>();
            try{
                Scanner s = new Scanner(new File(txtFile));

                while(s.hasNextLine()){
                    String line = s.nextLine();
                    //try to separate them and store them in a map
                    String[] value = line.split(" = ");
                    Map<String, Integer> nearbyCountryInfo = new HashMap<>(); 

                    //if have near countries
                    if(value.length > 1){
                        //delete the last three char: " km"
                        String changedStr = value[1].substring(0,value[1].length()-3);
                        //seperate the following string with " km; "
                        String[] namesDistances = changedStr.split(" km; ");
                        int i = 0;
                        while(i<namesDistances.length){
                            String[] tempND = namesDistances[i].split(" ");
                            String countryName = "";
                            if(tempND.length == 2){countryName = tempND[0];}
                            else{
                                int j=0;
                                for(j=0; j<tempND.length-2; j++){
                                    countryName += tempND[j] + " ";
                                }
                                countryName += tempND[j];
                            }
                            nearbyCountryInfo.put(countryName, Integer.MAX_VALUE);
                            i++;               
                        }
                    }

                    //store all information in this line to a hashmap
                    //key is countryName, value is another hashmap that stores nearby country information
                    linesInfo.put(value[0], nearbyCountryInfo);
                }
                s.close();
            }
            catch(Exception e2){System.err.println("Error2: " + e2.getMessage());System.exit(1);}
            return linesInfo;
    }
    
    //get two country codes, and distance(km)
    public Map<String,Map<String,Integer>> correctDistance(String csvFile){
        Map<String,Map<String,Integer>> linesInfo = new HashMap<>();
        try{
                Scanner s = new Scanner(new File(csvFile));
                //skip first line
                s.nextLine();
                while(s.hasNextLine()){
                    String line = s.nextLine();
                    //try to separate them and store them in a map
                    String[] value = line.split(",");

                    //check if the first country has already in this map
                    //if so, add second country's information to the inner map
                    if(linesInfo.containsKey(value[1])){
                        Map<String, Integer> temp = linesInfo.get(value[1]);
                        temp.put(value[3],Integer.parseInt(value[4]));
                    }
                    //if not, add information in the inner and outer map
                    else{
                        Map<String, Integer> temp = new HashMap<>();
                        temp.put(value[3],Integer.parseInt(value[4]));
                        linesInfo.put(value[1], temp);
                    }

                }
                s.close();
            }
            catch(Exception e3){System.err.println("Error3: " + e3.getMessage());System.exit(1);}
            return linesInfo;
    }


    public Map<String, String> countryToCode(String tsvFile){
        Map<String, String> country_code = new HashMap<>();
         try{
                Scanner s = new Scanner(new File(tsvFile));
                //skip first line
                s.nextLine();
                while(s.hasNextLine()){
                    String line = s.nextLine();
                    //try to separate them and store them in a map
                    String[] value = line.split("\t");
                    String stateid = value[1];
                    String countryname = value[2];
                    String date = value[4];
                    if(date.equals("2020-12-31")){
                        country_code.put(countryname, stateid);
                    }
                }
                s.close();
            }
            catch(Exception e4){System.err.println("Error4: " + e4.getMessage());System.exit(1);}
            country_code.put("Bahamas, The","BHM");
            country_code.put("Belarus","BLR");
            country_code.put("Bosnia and Herzegovina","BOS");
            country_code.put("Burkina Faso","BFO");
            country_code.put("Burma","MYA");
            country_code.put("Cabo Verde","CAP");
            country_code.put("Cambodia","CAM");
            country_code.put("Congo, Democratic Republic of the","DRC");
            country_code.put("Congo, Republic of the","CON");
            country_code.put("Cote d'Ivoire","CDI");
            country_code.put("Czechia","CZR");
            country_code.put("Democratic Republic of the Congo","DRC");
            country_code.put("Gambia, The","GAM");
            country_code.put("Germany","GFR");
            country_code.put("Iran","IRN");
            country_code.put("Italy","ITA");
            country_code.put("Korea, North","PRK");
            country_code.put("Korea, South","ROK");
            country_code.put("Kyrgyzstan","KYR");
            country_code.put("Lithuania (Kaliningrad Oblast)", "LIT");
            country_code.put("Macedonia","MAC");
            country_code.put("North Korea","PRK");
            country_code.put("North Macedonia","MAC");
            country_code.put("Republic of the Congo","CON");
            country_code.put("Russia","RUS");
            country_code.put("Russia (Kaliningrad)","RUS");
            country_code.put("South Korea","ROK");
            country_code.put("Sri Lanka","SRI");
            country_code.put("Suriname","SUR");
            country_code.put("Tanzania","TAZ");
            country_code.put("The Gambia","GAM");
            country_code.put("Timor-Leste","ETM");
            country_code.put("Turkey","TUR");
            country_code.put("Poland (Kaliningrad Oblast)", "POL");
            country_code.put("Turkey (Turkiye)","TUR");
            country_code.put("UAE","UAE");
            country_code.put("UK","UKG");
            country_code.put("United States","USA");
            country_code.put("US","USA");
            country_code.put("Vietnam","DRV");
            country_code.put("Yemen","YEM");
            country_code.put("Zimbabwe","ZIM");
            country_code.put("Spain (Ceuta)", "SPN");
            country_code.put("Morocco (Ceuta)","MOR");
            country_code.put("Eswatini", "SWA");
            country_code.put("Czechia", "CZR");
            country_code.put("Denmark (Greenland)", "DEN");

        return country_code;
    }

    private Map<String, String> fixedM(){

        fixedMap.put("Democratic Republic of the Congo","Congo, Democratic Republic of the");
        fixedMap.put("Republic of the Congo","Congo, Republic of the");
        fixedMap.put("Cote d'Ivoire","Cote D'Ivoire");
        fixedMap.put("Czech Republic","Czechia");
        fixedMap.put("Congo, Democratic Republic of (Zaire)","Democratic Republic of the Congo");
        fixedMap.put("Gambia, The","The Gambia");
        fixedMap.put("German Federal Republic","Germany");
        fixedMap.put("Iran (Persia)","Iran");
        fixedMap.put("Italy/Sardinia","Italy");
        fixedMap.put("Korea, Republic of","Korea, South");
        fixedMap.put("Kyrgyz Republic","Kyrgyzstan");
        fixedMap.put("Macedonia (Former Yugoslav Republic of)","Macedonia");
        fixedMap.put("North Macedonia","Macedonia");
        fixedMap.put("Korea, People's Republic of","Korea, North");
        fixedMap.put("North Korea","Korea, North");
        fixedMap.put("South Korea","Korea, South");
        fixedMap.put("Congo","Republic of the Congo");
        fixedMap.put("Russia (Kaliningrad)", "Russia");
        fixedMap.put("Lithuania (Kaliningrad Oblast)", "Lithuania");
        fixedMap.put("Poland (Kaliningrad Oblast)", "Poland");
        fixedMap.put("Russia (Kaliningrad Oblast)", "Poland");
        fixedMap.put("South Korea","Korea, South");
        fixedMap.put("Sri Lanka (Ceylon)","Sri Lanka");
        fixedMap.put("Surinam","Suriname");
        fixedMap.put("Tanzania/Tanganyika","Tanzania");
        fixedMap.put("The Gambia","Gambia, The");
        fixedMap.put("East Timor","Timor-Leste");
        fixedMap.put("Turkey","Turkey (Turkiye)");
        fixedMap.put("United Arab Emirates","UAE");
        fixedMap.put("UK","United Kingdom");
        fixedMap.put("US","United States");
        fixedMap.put("Macedonia", "North Macedonia");
        fixedMap.put("Morocco (Ceuta)", "Morocco");
        fixedMap.put("Spain (Ceuta)", "Spain");
        fixedMap.put("Denmark (Greenland)", "Denmark");
        return fixedMap;
    }

    public void acceptUserInput() {
        Scanner s = new Scanner(System.in);
        int flag = 0;
        int flag2 = 1;
        String country1 = "",country2 = "";
        String lineValue = "";
        while(flag2==1 || !lineValue.equals("EXIT")) {
            while(flag == 0){
                System.out.print("Enter the name of the first country (type EXIT to quit): ");
                lineValue = s.nextLine();
                if(lineValue.equals("EXIT")){break;}
                if(fixedMap.containsKey(lineValue)){
                    lineValue = fixedMap.get(lineValue);
                }
                if(!finalInfo.containsKey(lineValue)){
                    System.out.println("Invalid country name. Please enter a valid country name.");
                }
                else{flag = 1; country1 = lineValue;}
            }
            
            while(flag == 1){
                System.out.print("Enter the name of the second country (type EXIT to quit): ");
                lineValue = s.nextLine();
                if(lineValue.equals("EXIT")){break;}
                if(fixedMap.containsKey(lineValue)){
                    lineValue = fixedMap.get(lineValue);
                }
                if(!finalInfo.containsKey(lineValue)){
                    System.out.println("Invalid country name. Please enter a valid country name.");
                }
                else{
                    flag = 0;
                    country2 = lineValue;
                    //find shortest path
                    System.out.println("Route from "+country1+" to "+country2+":");
                    List<String> a = findPath(country1, country2);
                    for(String tempS : a){
                        System.out.println(tempS);
                    }
                }
            }
            flag2=0;
        }

        s.close();
    }

    
    public static void main(String[] args) {
        IRoadTrip a3 = new IRoadTrip(args);
        a3.acceptUserInput();
    }
}
