import sys,os
from PIL import Image, ImageDraw
out=sys.argv[1]; fs=sys.argv[2:]
W=10; im=Image.new('RGBA',(W*72,((len(fs)+W-1)//W)*72),(50,50,50,255)); d=ImageDraw.Draw(im)
for i,f in enumerate(fs):
    t=Image.open(f).convert('RGBA'); 
    t=t.resize((48,48*t.size[1]//t.size[0]) if t.size[0]<=64 else (48,48),Image.NEAREST); t=t.crop((0,0,48,48)); x=(i%W)*72; y=(i//W)*72
    im.paste(t,(x+12,y+2),t); d.text((x+1,y+54),os.path.basename(f)[:-4][:12],fill='white')
im.save(out)
