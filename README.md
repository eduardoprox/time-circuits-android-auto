# Time Circuits no Android Auto

O painel de tempo do DeLorean na central do carro, desenhado nativamente em Kotlin. Projeto pessoal.

- **DESTINATION TIME:** fixo em OCT 26 1985, 01:21 AM.
- **PRESENT TIME:** data e hora do celular.
- **LAST TIME DEPARTED:** fixo em OCT 26 1985, 01:20 AM.

## Instalar no celular

Requer **Android 15 ou superior**.

1. No celular, baixe o [APK da versão mais recente](https://github.com/eduardoprox/time-circuits-android-auto/releases/latest/download/time-circuits.apk).
2. Abra o arquivo baixado. Se o Android pedir, permita a instalação por esse app.
3. Abra **Time Circuits** para ver o painel em tela cheia no celular.

O APK é de teste, assinado com chave de depuração. Por isso, o Android ou o Play Protect podem mostrar um aviso.

## Como funciona no carro

O app usa a Car App Library na categoria navegação, a que permite desenhar numa superfície própria. Com isso, ele continua na tela com o carro andando (confirmado no DHU com `restrict all`) e também aparece como cartão na tela inicial, ao lado da música.

O modelo de navegação exige um botão. O único é **Noite/Dia**, que escurece o painel. O sistema o esconde depois de 10 segundos sem toque, e ele fica por cima do painel enquanto aparece.

No celular, o mesmo painel abre em tela cheia pelo ícone comum.

## Carro real

Segundo a documentação, a opção "Fontes desconhecidas" do Android Auto não cobre a Car App Library. Para o carro real, o caminho documentado é publicar numa faixa de teste interno do Google Play, que conta como fonte confiável e não passa por revisão. A categoria é declarada por necessidade técnica: o app não navega, então não serve para publicação aberta.

## Build

Com `JAVA_HOME` apontando para `C:\Program Files\Android\Android Studio\jbr`:

```
gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Testar no DHU

1. No Android Auto do celular, toque nos três pontinhos e em **Iniciar servidor da unidade principal**.
2. No computador, rode `adb forward tcp:5277 tcp:5277` e abra `desktop-head-unit.exe`, em `%LOCALAPPDATA%\Android\Sdk\extras\google\auto`.
3. Abra **Time Circuits** na lista de apps do DHU.

No console do DHU, `restrict all` simula o carro andando e `restrict none` volta ao normal.

**Depois de reinstalar:** se o relógio parar de atualizar e o log mostrar `superficie indisponivel`, feche e abra o DHU. Isso apareceu ao trocar a versão com o app aberto, porque o Android Auto continuou usando a superfície antiga.

Log do app: `adb logcat -s TimeCircuits`.

## Código

- `ClockCarAppService.kt`: entrada do app no Android Auto.
- `ClockScreen.kt`: tela do carro, com a superfície, o botão e a atualização a cada segundo.
- `PanelRenderer.kt`: layout e desenho do painel. As datas fixas ficam em `dest` e `last`.
- `Segments.kt`: geometria dos segmentos e das letras dos meses.
- `ClockActivity.kt` e `CircuitsView.kt`: o painel na tela do celular.

Na superfície do carro, use `lockCanvas`. O `lockHardwareCanvas` derrubou o processo ao trocar entre tela cheia e cartão (`getFrame() called on a context with no surface`).
