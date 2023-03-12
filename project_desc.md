# Description du projet :

Projet de surveillance en temps réel des menaces potentielles en utilisant les caméras déjà installées dans la ville.

Notre solution consiste en deux parties principales : **le backend et le frontend**. Le backend est responsable de la capture d'images, de l'analyse des données et de la gestion des événements, tandis que le frontend fournit une interface utilisateur intuitive pour visualiser les résultats en temps réel.

Dans le backend, nous prévoyons de mettre en place un service de capture de photos à intervalles réguliers à partir des caméras déjà installées dans la ville. Les images capturées seront envoyées à une IA pour détecter les menaces potentielles. Si une menace est détectée, notre système traitera l'information pour extraire les données pertinentes et envoyer une notification en temps réel via *Websocket* au système de gestion des événements. Les données des événements seront également stockées dans une base de données pour la sauvegarde et la création d'un historique. 
En outre, nous prévoyons de développer un panneau d'administration pour afficher les événements détectés, les coordonnées GPS et les actions à prendre.

Dans le frontend, nous prévoyons de créer une carte en temps réel pour afficher les événements détectés avec des marqueurs et des périmètres de danger. Les utilisateurs pourront facilement visualiser les événements en temps réel et prendre les mesures nécessaires pour résoudre les problèmes détectés.

**Backend 1 :**  Un service de capture de photos à intervalles est créé pour capturer les images à partir de la caméra. L'image capturée est envoyée à l'IA pour la reconnaissance d'objet afin de détecter les menaces. La réponse de l'IA est analysée et traitée pour extraire les informations pertinentes. Si une menace est confirmée, le système envoie une notification en temps réel au système de gestion des événements par Websocket au format JSON.

**Backend 2 :** Le système de gestion des événements reçoit les notifications en temps réel via Websocket. Les informations des événements détectés sont stockées dans une base de données pour la sauvegarde et la création d'un historique. Un panneau d'administration est créé pour afficher les menaces détectées, les coordonnées GPS et les actions à prendre.

**Frontend :** Une carte en temps réel est créée pour afficher les événements détectés. Les coordonnées GPS des événements détectés sont utilisées pour afficher les marqueurs sur la carte avec des périmètres de danger.