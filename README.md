bnmstest
========

for starting it up:
add these VM parameters: -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -XX:MaxPermSize=128M
for intellij idea in Project Structure -> Artifacts: change output directory to target of your application, like this: /home/araz/Projects/bourse/bnmstest/target
in output layout remove everything and just add bnmstest:war exploded to the <output root> (from the right column)
set(tik) build artifacts and select "bnmstest:war exploded" in you application run configuration
