
for i in cxxx #auxmem bank-memory #language-card
do
   acme  -o $i.bin -r $i.lst -f plain $i.s && cat $i.lst
done

#cat bank-memory.lst
#cat language-card.lst

