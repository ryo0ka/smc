Person
======
- CTF map builder
- with laptop that can't run client/server at the same time
- uses heavily Bukkit WorldEdit and sometimes MCEdit
- focuses on easiness, quick-access and enjoyment

WorldEdit
================
- the best range of tools
- easy to use once knowing the syntax and nuances
- easy to plugin to servers
- not too hard to pickup
- f: travel tools (left-click a block to teleport there)
- f: selection tools (3d selection, copy/paste, replace, etc)
- f: brush tools
- **not made for huge edits** (like mirroring 200x200 blocks)
    - **how?**

WorldEdit Server
==================
- updated regularly
- installs WorldEdit and utilities (like multi-worlds and warps)
- no need for installing/maintaining plugins, compatibility and stuff
- **need to be careful not to crush the server**
    - why not client-side version? → it takes extra effort for installing/maintenance
    - implementing a queue system for operations
- **takes a personal request to pull/push world segments**
    - reputation system
    - utility like the Git system

MCEdit
======
- great for large operations (flipping) that WorldEditor may crush the server
- RAM use hasn't been an issue
- clunky performance and interface
- tedious to leave Minecraft and open another program
- only used for filters that WorldEditor lacks (jpeg to terrain) and for unattainable blocks

Raw answers
===========

Who are you?
------------
I am a veteran map-maker. I'm quite familiar with WorldEditer, MCEdit, a little Voxel Sniper, and WorldPainter.

About WorldPainter
------------------
Really only for large-scale terra-forming and land generation.  
It's very rare that I use it, and if I do, it's not during the map-making process.

About MCEdit
------------
By this, I assume that you mean the Python-app built by codewarrior.  
Because of it's hard to use interface, and speed, I only use MCEdit for it's filters, which WorldEdit does not have.  
For instance, one of the only ways I found of converting an image to an ingame map was through an MCedit filter.  
It's also great for things like Flipping large builds, because you don't have to be active inside the game to use it,  
which means you aren't as likely to crash due to strain on the server.

About WorldEdit
---------------
Definitely my favorite. It's really easy to use once you know the syntax and nuances.  
Although there are more advanced editors like VoxelSniper,  
WE is easy to plugin to servers, and not too hard to pickup, and has a good range of tools.

The 3 things that make it great are:

1. Its fast travel system (even though flying in creative is fast,  
   when building on anything bigger than a large house,  
   it's relatively inefficient) WorldEdit offers a way to "Teleport" to any block  
   within viewing distance by left clicking with a compass at any block in view.
2. Its "Selection" commands, //pos1, and //pos2.  
   With those, you can "select" a 3d volume, and manipulate it however you like.  
   You can replace all the lava within an area with air,  
   or randomly replacing a field of boring dirt with a mixture of stone and green wool.  
   Or, you can select a nice house, copy it, and paste it a hundred times to make a town.  
   If anything, this makes WE great.
3. Its brush system. Any non-block item can be assigned a brush and block type.  
   Useful for minor terraforming or bulk filler.

However, as mentioned above, I don't really make Adventure Maps.  
I make PvP maps that often require two or even 4 identical areas.  
Take for instance MCPVP's CTF servers. Although maps can be small,  
it's not easy taking a huge 200x200 area within the game and flipping it to match the other area.  
WE was not made for huge edits, and MCedit is sometimes slow and unreliable.

Why is WorldEdit sometimes not reliable to you?
--------------------------------------------
WorldEdit is powerful when you're using individual brushes like //brush sphere or a //repl brush.  
But when you're trying to do large scale edits like mirroring an entire map, it can crash the server,  
depending on who else is on the server. I use the MCPVP build servers, and on average,  
there are around 20~ people on at a time, and the server  
will become visibly laggy whenever some fool is using WE too much.  
So, it excels as apersonal, precise tool, but doesn't have the power to do large scale edits.

Are you working on your projects in a server? If so, why?
---------------------------------------------------------
As mentioned above, I'm currently using the MCPVP build servers to do everything with WE.  
Although I could use SPC and install WE as a LAN mod, I've found in the past that it was choppy,  
and my laptop couldn't handle running a server simultaneously with the client.  
Also, the MCPVP build servers are updated regularly, have a nice host of other plugins (like multi-worold and /warp)  
that aid the build-process nicely.  
The only problem i've found with them is that it takes a  
personal request to pull your world from their servers, and vice versa.

The other main thing is that **there really are no single-player mods that have the capabilities of WE**,  
and MCedit was never made for hand-building. It's more of a utility for specialized filters, or unattainable blocks.

Do you often import schematic files from 3rd-party tools into WorldEdit?
------------------------------------------------------------------------
Actually, yes. Not often though. I've done it a couple times when I had a concrete visualization of the terrain,   
and reproduced it in WorldPainter, but other wise, it's usually WorldEdit.   
If you go slow, you can lay down huge chunks of land, and slowly carve it into what you want, but again,   
its a lot of strain on the server, which means you have to go a lot slower.

That's for world files though. As for .schematics, I don't actually use them very often.
i don't know what or how other people use them,   
but I know WE has a simple way to load/save/import selections as .schematics,   
which is a very nice way of backing up individual (though small) builds for copy/pasting or even as a backup.

Do you think the client-version WorldEdit may solve any parts of your problem?
------------------------------------------------------------------------------
Being a somewhat lazy guy, I don't like having to wait for mods to update,  
and I found it easier to rely on the server for the mods. The main factor was **ease-of-use**. There was  
- having to download and install a mod that might not work (With myself not being too familiar with mods)  
- Simply joining a server and having instant access to a wide range of tools,  
and other side utilities that I didn't have to pay attention to, nor maintain.

MCEdit can perform mirror. Does the method takes too much time? Or RAM capacity issue?
--------------------------------------------------------------------------------------------
Yes, MCedit can mirror things, and with a fairly small amount of steps, but again,  
the clunkiness of MCEdit and the effort of having to leave Minecraft, go to a seperate program,  
and then go back was always tedious. I used this method for a few maps, but eventually,  
on servers in which you don't have immediate access to the actual world file, I had to use WE,  
which was even more tedious (though more hands on). RAM was never really an issue.

Is the "hand-building" important for you?
-----------------------------------------
Hand building is definitely and important aspect of this. The main driving point is that all/most builders build **for fun,  
for entertainment**, and tools that are clunky or hard to use just take the fun out of it.  
Not too say that WE isn't part of that hand-building part. It simply accelerates the process.  
It's fun building the intricate supports and pillars of a rail-bridge, but having to build it 100 more times is not fun.  
So, yes,, hand-building is very important, and mods can sometimes be a part of that handbuilding process.