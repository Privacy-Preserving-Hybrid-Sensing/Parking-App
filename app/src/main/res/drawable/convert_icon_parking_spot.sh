ls incentive_*.png|while read baris; do convert -resize 32x44 $baris $baris; done
ls participate_*.png|while read baris; do convert -resize 32x44 $baris $baris; done
ls default_*.png|while read baris; do convert -resize 32x44 $baris $baris; done
