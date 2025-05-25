"Application de Traitement d’Images en JavaFX"  
  description: >  
    Ce projet est une application desktop développée en JavaFX pour le traitement d’images : importation, ajout de bruit, débruitage et affichage des résultats avec des métriques.  

  arborescence:  
    description: "Arborescence du projet"  
    structure: 
  Projet-Java  
  ├── analyse  
  │   ├── diagrammePNG  
  │   ├── diagrammePUML   
  │   ├── feedback   
  │   ├── gantt   
  │   └── maquette  
  ├── Application.jar   
  ├── Groupe11_Livrable1.pdf  
  ├── Groupe11_Livrable3  
  │   ├── class.png  
  │   ├── content  
  │   ├── main.bcf  
  │   ├── main.pdf  
  │   ├── main.run.xml  
  │   ├── main.tex  
  │   ├── reference  
  │   └── setting  
  ├── Groupe11_Livrable3.pdf  
  ├── projet  
  │   ├── images  
  │   ├── lib  
  │   └── src  
  ├── README.md  
  └── sources.jar  

    explications:  
      - "analyse/ : diagrammes UML, planning, maquette UI."    
      - "projet/images/ : images sources, bruitées, résultats."    
      - "projet/lib/ : bibliothèques tierces (ex : commons-math)."  
      - "projet/src/ : code source Java."  

  prerequis:   
    - "Java JDK 11 minimum (Java 17 recommandé)."   
    - "JavaFX SDK 17 ou plus récent (disponible sur https://openjfx.io/)."   
    - "Système d’exploitation : Linux, Windows, macOS."   

  compilation_et_jar:  
    etapes:   
      - "Se positionner dans le dossier projet : cd projet"   
      - "Compiler le projet :"   
      - "  Sous Linux/macOS :"   
      - "    javac -d out --module-path /chemin/vers/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml src/*.java"    
      - "  Sous Windows (PowerShell) :"    
      - "    javac -d out --module-path C:\\chemin\\vers\\javafx-sdk\\lib --add-modules javafx.controls,javafx.fxml src\\*.java"     
      - "Créer un fichier manifeste manifest.txt avec :"    
      - "  Main-Class: Main"   
      - "Créer le JAR exécutable :"    
      - "  jar cfm MaquetteApp.jar manifest.txt -C out ."    

  execution:    
    instructions:   
      - "Sous Linux/macOS :"    
      - "  java --module-path /chemin/vers/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -jar MaquetteApp.jar"  
      - "Sous Windows (PowerShell) :"     
      - "  java --module-path C:\\chemin\\vers\\javafx-sdk\\lib --add-modules javafx.controls,javafx.fxml -jar MaquetteApp.jar"    

  utilisation:   
    description: >   
      - Cliquer sur **Importer** pour charger une image.   
      - Appliquer du bruit ou un débruitage via les contrôles.   
      - Visualiser les images originales, bruitées et débruitées.    
      - Consulter les métriques affichées.   

  dependances_externes:    
    - "Apache Commons Math 3.6.1 (https://commons.apache.org/proper/commons-math/)"    
    - "Placée dans projet/lib/commons-math3-3.6.1.jar"    
    - "À inclure dans compilation et exécution si utilisé."    


    - "Projet réalisé par le groupe 11."   

