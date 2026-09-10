from pathlib import Path
rows = '''title|EXPEDITION ATLAS|ATLAS DES EXPÉDITIONS|ATLAS DE EXPEDICIONES|ATLAS EXPEDÍCIÍ
search|Search expeditions, loot, creatures...|Chercher expéditions, butin, créatures...|Buscar expediciones, botín, criaturas...|Hľadať expedície, korisť, tvory...
close|Close|Fermer|Cerrar|Zavrieť
tab.explore|EXPLORE|EXPLORER|EXPLORAR|OBJAVOVAŤ
tab.expeditions|EXPEDITIONS|EXPÉDITIONS|EXPEDICIONES|EXPEDÍCIE
tab.loot|LOOT|BUTIN|BOTÍN|KORISŤ
tab.dungeons|DUNGEONS|DONJONS|MAZMORRAS|ŽALÁRE
frontier|YOUR FRONTIER|VOTRE FRONTIÈRE|TU FRONTERA|TVOJA HRANICA
searchResults|SOURCE EXPEDITIONS|EXPÉDITIONS SOURCES|EXPEDICIONES DE ORIGEN|ZDROJOVÉ EXPEDÍCIE
featured|FEATURED LOOT|BUTIN PRINCIPAL|BOTÍN DESTACADO|VYBRANÁ KORISŤ
enemies|ENEMY WAVES|VAGUES D'ENNEMIS|OLEADAS ENEMIGAS|VLNY NEPRIATEĽOV
fullLoot|Show all loot / waves|Voir tout le butin / les vagues|Ver todo el botín / oleadas|Zobraziť všetku korisť / vlny
hideLoot|Hide full list|Masquer la liste|Ocultar lista|Skryť zoznam
route|LOCAL ROUTE|PARCOURS LOCAL|RUTA LOCAL|MIESTNA TRASA
parents|FROM|DEPUIS|DESDE|ODKIAĽ
selected|SELECTED|SÉLECTION|SELECCIÓN|VÝBER
children|NEXT|ENSUITE|SIGUIENTE|ĎALEJ
rewards|COMPLETION REWARDS|RÉCOMPENSES DE FIN|RECOMPENSAS FINALES|ZÁVEREČNÉ ODMENY
craft|CRAFT CRYSTAL|CRÉER LE CRISTAL|CREAR CRISTAL|VYROBIŤ KRYŠTÁL
refresh|Refresh materials|Actualiser les matériaux|Actualizar materiales|Obnoviť materiály
cost|Crystal recipe materials (inventory and accessible nearby chests)|Matériaux de la recette (inventaire et coffres proches accessibles)|Materiales (inventario y cofres cercanos accesibles)|Materiály (inventár a dostupné okolité truhlice)
lock|LOCK|VERROU|CERRADO|ZÁMOK
filter.all|All states|Tous les états|Todos los estados|Všetky stavy
filter.unlocked|Unlocked|Débloquées|Desbloqueadas|Odomknuté
filter.locked|Locked|Verrouillées|Bloqueadas|Zamknuté
filter.affordable|Unlocked + affordable|Débloquées + abordables|Desbloqueadas + asequibles|Odomknuté + dostupné
filter.tiers|All tiers|Tous les paliers|Todos los niveles|Všetky úrovne
filter.categories|All difficulties|Toutes les difficultés|Todas las dificultades|Všetky náročnosti
tier|Tier {tier}|Palier {tier}|Nivel {tier}|Úroveň {tier}
blocks|{count} blocks|{count} blocs|{count} bloques|{count} blokov
waves|{count} waves|{count} vagues|{count} oleadas|{count} vĺn
wave|Wave {wave} · ×{count}|Vague {wave} · ×{count}|Oleada {wave} · ×{count}|Vlna {wave} · ×{count}
discovered|{found} / {total} paths discovered|{found} / {total} chemins découverts|{found} / {total} rutas descubiertas|{found} / {total} objavených ciest
sources|{count} source expeditions|{count} expéditions sources|{count} expediciones de origen|{count} zdrojových expedícií
results|{count} results|{count} résultats|{count} resultados|{count} výsledkov
yield|{chance} per block · {duration} blocks · ≈{expected} total|{chance} par bloc · {duration} blocs · ≈{expected} au total|{chance} por bloque · {duration} bloques · ≈{expected} total|{chance} na blok · {duration} blokov · ≈{expected} celkom
rewardChance|×{count} · {chance} on completion|×{count} · {chance} en fin d'expédition|×{count} · {chance} al completar|×{count} · {chance} po dokončení
unlockChance|{chance} unlock chance on completion|{chance} de déblocage en fin d'expédition|{chance} de desbloqueo al completar|{chance} šanca odomknutia po dokončení
creature|CREATURE|CRÉATURE|CRIATURA|TVOR
item|ITEM|OBJET|OBJETO|PREDMET
tool.Hand|Hand|Main|Mano|Ruka
tool.Pickaxe|Pickaxe|Pioche|Pico|Krompáč
tool.Axe|Axe|Hache|Hacha|Sekera
state.unlocked|Recipe unlocked|Recette débloquée|Receta desbloqueada|Recept odomknutý
state.locked|Locked|Verrouillée|Bloqueada|Zamknuté
state.ready|Unlocked · affordable|Débloquée · abordable|Desbloqueada · asequible|Odomknuté · dostupné
state.materials|Unlocked · missing materials|Débloquée · matériaux manquants|Desbloqueada · faltan materiales|Odomknuté · chýbajú materiály
state.active|Currently active|Actuellement active|Activa actualmente|Práve aktívne
empty.frontier|No frontier expeditions. Complete expeditions to discover new paths; fully discovered branches and endpoints are hidden here.|Aucune expédition à explorer. Terminez des expéditions pour découvrir de nouveaux chemins ; les branches entièrement découvertes sont masquées.|No hay expediciones fronterizas. Completa expediciones para descubrir rutas; las ramas completas y destinos finales se ocultan aquí.|Žiadne hraničné expedície. Dokonči expedície a objav nové cesty; dokončené vetvy a koncové body sú tu skryté.
empty.search|No matching expeditions. Try another search or filter.|Aucune expédition correspondante. Essayez une autre recherche ou un autre filtre.|Sin coincidencias. Prueba otra búsqueda o filtro.|Žiadne zodpovedajúce expedície. Skús iné hľadanie alebo filter.
empty.selection|Select an expedition or search for loot to see its sources.|Sélectionnez une expédition ou cherchez un butin pour voir ses sources.|Selecciona una expedición o busca botín para ver sus fuentes.|Vyber expedíciu alebo hľadaj korisť a zobraz jej zdroje.
craftState.ready|Recipe unlocked. Ready to craft.|Recette débloquée. Prêt à fabriquer.|Receta desbloqueada. Listo para crear.|Recept odomknutý. Pripravené na výrobu.
craftState.locked|Discover this crystal through its unlock route first.|Découvrez ce cristal via son parcours de déblocage.|Descubre primero este cristal siguiendo su ruta.|Najprv objav tento kryštál cez jeho trasu odomknutia.
craftState.materials|Missing recipe materials.|Matériaux de recette manquants.|Faltan materiales.|Chýbajú materiály receptu.
craftState.capacity|Inventory full. Make space for every crystal first.|Inventaire plein. Libérez de la place pour tous les cristaux.|Inventario lleno. Haz espacio para todos los cristales.|Plný inventár. Uvoľni miesto pre všetky kryštály.
craftState.station|Use the matching enchanter nearby. Dungeon crystals require the Dungeon Enchanter; normal crystals require the Crystal Enchanter.|Utilisez l'enchanteur adapté à proximité : enchanteur de donjon pour les donjons, enchanteur de cristaux pour les autres.|Usa el encantador adecuado cercano: el de mazmorras para sus cristales y el normal para los demás.|Použi správny blízky očarovací stôl: žalárový pre žalárové kryštály, bežný pre ostatné.
craftState.invalid|Expedition or recipe unavailable. Reopen the Atlas.|Expédition ou recette indisponible. Rouvrez l'atlas.|Expedición o receta no disponible. Abre de nuevo el atlas.|Expedícia alebo recept nie je dostupný. Znovu otvor atlas.
craftState.quantity|Choose between 1 and 64 crystals.|Choisissez entre 1 et 64 cristaux.|Elige entre 1 y 64 cristales.|Vyber 1 až 64 kryštálov.
craftState.stale|Selection changed. Check the quantity before crafting again.|La sélection a changé. Vérifiez la quantité avant de réessayer.|La selección cambió. Revisa la cantidad antes de crear de nuevo.|Výber sa zmenil. Pred ďalšou výrobou skontroluj množstvo.
craftState.failed|Crafting was refused or failed. Refresh and check the recipe and station requirements.|Fabrication refusée ou échouée. Actualisez et vérifiez les conditions de recette et d'atelier.|Creación rechazada o fallida. Actualiza y comprueba los requisitos.|Výroba bola odmietnutá alebo zlyhala. Obnov a skontroluj požiadavky receptu a stola.
craftState.crafted|Crafting completed.|Fabrication terminée.|Creación completada.|Výroba dokončená.
craftState.timed|This modified recipe requires timed crafting, which is unavailable in the Atlas.|Cette recette modifiée nécessite une fabrication temporisée, indisponible dans l'atlas.|Esta receta modificada requiere creación temporizada, no disponible en el atlas.|Tento upravený recept vyžaduje časovanú výrobu, ktorá nie je dostupná v atlase.
'''
for index, language in enumerate(['en-US','fr-FR','es-ES','sk-SK'], 1):
    p = Path(f'mods/oneblock/src/main/resources/Server/Languages/{language}/server.lang')
    text = p.read_text(encoding='utf-8').rstrip() + '\n\n# Expedition Atlas\n'
    for row in rows.strip().splitlines():
        values = row.split('|')
        assert len(values) == 5, row
        text += f'atlas.{values[0]}={values[index]}\n'
    p.write_text(text, encoding='utf-8')
