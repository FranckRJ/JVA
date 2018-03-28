# Informations pour forker le projet

Le repo git ne contient que les sources / ressources du projet ainsi que quelques autres fichiers, et non tout le projet Android Studio en entier. Pour exemple voici le chemin du root du repo git par rapport au root du projet Android Studio `JVA/app/src/main/`.

Pour avoir un projet fonctionnel vous devrez en créer un nouveau depuis Android Studio peu importe les options, remplacer le contenu du dossier `<NOM_DU_PROJET>/app/src/main/` par celui du repo git, puis remplacer les fichiers suivants :
* `<NOM_DU_PROJET>/build.gradle` par le fichier `jva.build.gradle` du repo git (le fichier doit être renommé `build.gradle`).
* `<NOM_DU_PROJET>/app/build.gradle` par le fichier `app.build.gradle` du repo git (le fichier doit être renommé `build.gradle`).
* `<NOM_DU_PROJET>/app/proguard-rules.pro` par le fichier `proguard-rules.pro` du repo git.