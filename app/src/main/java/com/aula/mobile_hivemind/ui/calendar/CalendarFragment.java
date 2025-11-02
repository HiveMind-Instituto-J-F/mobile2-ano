package com.aula.mobile_hivemind.ui.calendar;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aula.mobile_hivemind.api.RetrofitClient;
import com.aula.mobile_hivemind.api.SqlApiService;
import com.aula.mobile_hivemind.databinding.FragmentCalendarBinding;
import com.aula.mobile_hivemind.dto.ParadaSQLResponseDTO;
import com.aula.mobile_hivemind.dto.RegistroParadaResponseDTO;
import com.aula.mobile_hivemind.utils.CustomToast;
import com.aula.mobile_hivemind.recyclerViewParadas.Parada;
import com.aula.mobile_hivemind.recyclerViewParadas.ParadaCalendarAdapter;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private ParadaCalendarAdapter paradaCalendarAdapter;
    private List<Parada> todasParadas;
    private SimpleDateFormat dateFormat;
    private SqlApiService sqlApiService;
    private com.aula.mobile_hivemind.api.ApiService mongoApiService;

    // Variáveis do usuário
    private String userType;
    private String userSetor;

    // Cores melhoradas para uma aparência mais profissional
    private static final int COR_EM_ANDAMENTO = Color.rgb(220, 53, 69);    // Vermelho mais suave
    private static final int COR_FINALIZADA = Color.rgb(40, 167, 69);      // Verde mais suave
    private static final int COR_MISTA = Color.rgb(111, 66, 193);          // Roxo elegante para misto
    private static final int COR_SELECAO = Color.rgb(23, 162, 184);        // Azul para seleção
    private static final int COR_TEXTO_PRIMARIO = Color.rgb(33, 37, 41);
    private static final int COR_TEXTO_SECUNDARIO = Color.rgb(108, 117, 125);
    private static final int COR_FUNDO_CALENDARIO = Color.rgb(248, 249, 250);

    // Mapas para controlar as datas
    private Set<CalendarDay> datasEmAndamento;
    private Set<CalendarDay> datasFinalizadas;
    private Set<CalendarDay> datasMistas;
    private CalendarDay dataSelecionadaAtual;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar APIs
        sqlApiService = RetrofitClient.getSqlApiService();
        mongoApiService = RetrofitClient.getApiService();
        dateFormat = new SimpleDateFormat("dd, MMM yyyy", Locale.getDefault());

        // Inicializar conjuntos de datas
        datasEmAndamento = new HashSet<>();
        datasFinalizadas = new HashSet<>();
        datasMistas = new HashSet<>();
        dataSelecionadaAtual = null;

        obterInformacoesUsuario();

        configurarAparenciaCalendario();

        todasParadas = new ArrayList<>();

        configurarRecyclerView();

        carregarTodasParadas();

        binding.calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                if (selected) {
                    dataSelecionadaAtual = date;
                    filtrarParadasPorData(date);
                    // Reaplicar todos os decoradores para atualizar a seleção
                    reaplicarDecoradores();
                } else {
                    dataSelecionadaAtual = null;
                    reaplicarDecoradores();
                }
            }
        });

        return root;
    }

    private void configurarAparenciaCalendario() {
        MaterialCalendarView calendarView = binding.calendarView;

        // Configurar título do mês em português com formatação melhorada
        calendarView.setTitleFormatter(month -> {
            SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", new Locale("pt", "BR"));
            String mesAno = format.format(month.getDate());

            // Criar texto com formatação especial
            SpannableString spannable = new SpannableString(mesAno);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new RelativeSizeSpan(1.2f), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new ForegroundColorSpan(COR_TEXTO_PRIMARIO), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            return spannable;
        });

        // Configurar dias da semana em português
        calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(
                new String[]{"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"}
        ));

        // Remover setas de navegação (como solicitado)
        calendarView.setLeftArrowMask(null);
        calendarView.setRightArrowMask(null);

        // Configurar cores e estilos
        calendarView.setBackgroundColor(COR_FUNDO_CALENDARIO);

        // Aplicar decoradores visuais
        calendarView.addDecorator(new DiaAtualDecorador());
        calendarView.addDecorator(new DiasOutrosMesesDecorador());

        // Configurar altura das linhas
        calendarView.setTileHeightDp(48);
    }

    private void reaplicarDecoradores() {
        // Limpar todos os decoradores
        binding.calendarView.removeDecorators();

        // Reaplicar decoradores base
        configurarAparenciaCalendario();

        // Reaplicar decoradores de eventos
        aplicarDecoradoresEventos();

        // Aplicar decorador de seleção se houver data selecionada
        if (dataSelecionadaAtual != null) {
            binding.calendarView.addDecorator(new SelecaoDecorador(dataSelecionadaAtual));
        }
    }

    private void configurarRecyclerView() {
        paradaCalendarAdapter = new ParadaCalendarAdapter(getContext(), todasParadas, sqlApiService);
        binding.recyclerViewCalendar.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewCalendar.setAdapter(paradaCalendarAdapter);

        // Adicionar divisor entre itens
        binding.recyclerViewCalendar.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(
                getContext(), LinearLayoutManager.VERTICAL));
    }

    private void obterInformacoesUsuario() {
        // Implemente conforme sua lógica de autenticação
        userType = "man"; // Engenheiro
        userSetor = "Montagem";
    }

    private void carregarTodasParadas() {
        Log.d("CalendarFragment", "=== INICIANDO CARREGAMENTO DE PARADAS ===");

        // Limpar dados anteriores
        todasParadas.clear();
        datasEmAndamento.clear();
        datasFinalizadas.clear();
        datasMistas.clear();
        dataSelecionadaAtual = null;

        // Limpar decoradores existentes
        binding.calendarView.removeDecorators();

        // Reaplicar decoradores base
        configurarAparenciaCalendario();

        // Carregar paradas do MongoDB (paradas em andamento) - VERMELHO
        carregarParadasMongoDB();

        // Carregar paradas do SQL (paradas finalizadas) - VERDE
        carregarParadasSQL();
    }

    private void carregarParadasMongoDB() {
        Log.d("CalendarFragment", "Buscando paradas EM ANDAMENTO do MongoDB...");

        Call<List<RegistroParadaResponseDTO>> call = mongoApiService.getAllRegistros();
        call.enqueue(new Callback<List<RegistroParadaResponseDTO>>() {
            @Override
            public void onResponse(Call<List<RegistroParadaResponseDTO>> call, Response<List<RegistroParadaResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RegistroParadaResponseDTO> paradasMongo = response.body();
                    Log.d("CalendarFragment", "Paradas EM ANDAMENTO encontradas: " + paradasMongo.size());

                    for (RegistroParadaResponseDTO registro : paradasMongo) {
                        Parada parada = converterParaParada(registro);
                        parada.setTipo("EM_ANDAMENTO");

                        if (deveMostrarParada(parada)) {
                            todasParadas.add(parada);
                            adicionarDataAoMapa(parada.getDt_parada(), "EM_ANDAMENTO");
                        }
                    }

                    aplicarDecoradoresEventos();
                    atualizarUI();

                } else {
                    Log.e("CalendarFragment", "Erro ao buscar paradas MongoDB: " + response.code());
                    mostrarMensagemErro("Erro ao carregar paradas em andamento");
                }
            }

            @Override
            public void onFailure(Call<List<RegistroParadaResponseDTO>> call, Throwable t) {
                Log.e("CalendarFragment", "Falha ao buscar paradas MongoDB: " + t.getMessage());
                mostrarMensagemErro("Falha na conexão com servidor");
            }
        });
    }

    private void carregarParadasSQL() {
        Log.d("CalendarFragment", "Buscando paradas FINALIZADAS do SQL...");

        Call<List<ParadaSQLResponseDTO>> call = sqlApiService.listarTodasParadas();
        call.enqueue(new Callback<List<ParadaSQLResponseDTO>>() {
            @Override
            public void onResponse(Call<List<ParadaSQLResponseDTO>> call, Response<List<ParadaSQLResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ParadaSQLResponseDTO> paradasSQL = response.body();
                    Log.d("CalendarFragment", "Paradas FINALIZADAS encontradas: " + paradasSQL.size());

                    for (ParadaSQLResponseDTO registro : paradasSQL) {
                        Parada parada = converterParaParada(registro);
                        parada.setTipo("FINALIZADA");

                        if (deveMostrarParada(parada)) {
                            todasParadas.add(parada);
                            adicionarDataAoMapa(parada.getDt_parada(), "FINALIZADA");
                        }
                    }

                    aplicarDecoradoresEventos();
                    atualizarUI();

                } else {
                    Log.e("CalendarFragment", "Erro ao buscar paradas SQL: " + response.code());
                    mostrarMensagemErro("Erro ao carregar paradas finalizadas");
                }
            }

            @Override
            public void onFailure(Call<List<ParadaSQLResponseDTO>> call, Throwable t) {
                Log.e("CalendarFragment", "Falha ao buscar paradas SQL: " + t.getMessage());
                mostrarMensagemErro("Falha na conexão com banco de dados");
            }
        });
    }

    private void adicionarDataAoMapa(Date data, String tipo) {
        if (data == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);
        CalendarDay dia = CalendarDay.from(calendar);

        if (tipo.equals("EM_ANDAMENTO")) {
            // Se já existe finalizada, mover para misto
            if (datasFinalizadas.contains(dia)) {
                datasFinalizadas.remove(dia);
                datasMistas.add(dia);
            } else if (!datasMistas.contains(dia)) {
                datasEmAndamento.add(dia);
            }
        } else if (tipo.equals("FINALIZADA")) {
            // Se já existe em andamento, mover para misto
            if (datasEmAndamento.contains(dia)) {
                datasEmAndamento.remove(dia);
                datasMistas.add(dia);
            } else if (!datasMistas.contains(dia)) {
                datasFinalizadas.add(dia);
            }
        }
    }

    private void aplicarDecoradoresEventos() {
        // Aplicar decoradores para cada tipo de data
        if (!datasEmAndamento.isEmpty()) {
            binding.calendarView.addDecorator(new EventoDecorador(
                    new ArrayList<>(datasEmAndamento),
                    criarDrawableEvento(COR_EM_ANDAMENTO),
                    "Em Andamento"
            ));
        }

        if (!datasFinalizadas.isEmpty()) {
            binding.calendarView.addDecorator(new EventoDecorador(
                    new ArrayList<>(datasFinalizadas),
                    criarDrawableEvento(COR_FINALIZADA),
                    "Finalizada"
            ));
        }

        if (!datasMistas.isEmpty()) {
            binding.calendarView.addDecorator(new EventoDecorador(
                    new ArrayList<>(datasMistas),
                    criarDrawableEvento(COR_MISTA),
                    "Mista"
            ));
        }

        // Aplicar decorador de seleção se houver data selecionada
        if (dataSelecionadaAtual != null) {
            binding.calendarView.addDecorator(new SelecaoDecorador(dataSelecionadaAtual));
        }

        // Adicionar legenda
        adicionarLegenda();
    }

    private Drawable criarDrawableEvento(int cor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(cor);
        drawable.setStroke(2, Color.WHITE);
        drawable.setSize(36, 36);
        return drawable;
    }

    private Drawable criarDrawableEventoComSelecao(int corEvento, boolean selecionado) {
        if (selecionado) {
            // Criar layer drawable com evento + borda de seleção
            GradientDrawable eventoDrawable = new GradientDrawable();
            eventoDrawable.setShape(GradientDrawable.OVAL);
            eventoDrawable.setColor(corEvento);
            eventoDrawable.setSize(36, 36);

            GradientDrawable selecaoDrawable = new GradientDrawable();
            selecaoDrawable.setShape(GradientDrawable.OVAL);
            selecaoDrawable.setColor(Color.TRANSPARENT);
            selecaoDrawable.setStroke(3, COR_SELECAO);
            selecaoDrawable.setSize(42, 42);

            Drawable[] layers = {eventoDrawable, selecaoDrawable};
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            layerDrawable.setLayerInset(0, 3, 3, 3, 3); // Centralizar o evento dentro da seleção

            return layerDrawable;
        } else {
            return criarDrawableEvento(corEvento);
        }
    }

    private void adicionarLegenda() {
        // Verificar se o container de legenda existe no layout
        if (binding.containerLegenda == null) {
            return;
        }

        View legendaView = getLayoutInflater().inflate(com.aula.mobile_hivemind.R.layout.legenda_calendario,
                binding.containerLegenda, false);

        TextView legendaMongo = legendaView.findViewById(com.aula.mobile_hivemind.R.id.legenda_mongo);
        TextView legendaSQL = legendaView.findViewById(com.aula.mobile_hivemind.R.id.legenda_sql);
        TextView legendaMista = legendaView.findViewById(com.aula.mobile_hivemind.R.id.legenda_mista);

        if (legendaMongo != null) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(COR_EM_ANDAMENTO);
            drawable.setSize(20, 20);
            legendaMongo.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }

        if (legendaSQL != null) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(COR_FINALIZADA);
            drawable.setSize(20, 20);
            legendaSQL.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }

        if (legendaMista != null) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(COR_MISTA);
            drawable.setSize(20, 20);
            legendaMista.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }

        // Adicionar legenda ao layout
        binding.containerLegenda.removeAllViews();
        binding.containerLegenda.addView(legendaView);
    }

    // MÉTODOS DE CONVERSÃO E VALIDAÇÃO
    private Parada converterParaParada(RegistroParadaResponseDTO registro) {
        Parada parada = new Parada(
                registro.getId(),
                registro.getId_maquina(),
                registro.getId_usuario(),
                registro.getDes_parada(),
                registro.getDes_setor(),
                registro.getDt_parada(),
                registro.getHora_Fim(),
                registro.getHora_Inicio()
        );
        parada.setTipo("EM_ANDAMENTO");
        return parada;
    }

    private Parada converterParaParada(ParadaSQLResponseDTO registro) {
        Parada parada = new Parada(
                String.valueOf(registro.getId_registro_paradas()), // ID do SQL
                registro.getId_maquina(),
                registro.getId_usuario(),
                registro.getDes_parada(),
                registro.getDes_setor(),
                converterStringParaDate(registro.getDt_parada()), // Converter data
                converterStringParaDate(registro.getHora_fim()),   // Converter hora fim
                converterStringParaDate(registro.getHora_inicio()) // Converter hora início
        );
        parada.setTipo("FINALIZADA");
        return parada;
    }

    private Date converterStringParaDate(String dataString) {
        if (dataString == null || dataString.isEmpty()) {
            return null;
        }

        try {
            // Tente diferentes formatos baseado no seu SQL
            SimpleDateFormat[] formatos = {
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                    new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            };

            for (SimpleDateFormat formato : formatos) {
                try {
                    return formato.parse(dataString);
                } catch (java.text.ParseException e) {
                    // Continua para o próximo formato
                }
            }

            Log.e("CalendarFragment", "Não foi possível converter: " + dataString);
            return null;
        } catch (Exception e) {
            Log.e("CalendarFragment", "Erro ao converter: " + dataString, e);
            return null;
        }
    }

    private boolean deveMostrarParada(Parada parada) {
        // Engenheiro (MOP) vê todas as paradas
        if ("MOP".equals(userType)) {
            return true;
        }

        // Operador (regular) vê apenas paradas do seu setor
        if ("regular".equals(userType)) {
            return userSetor != null && userSetor.equals(parada.getDes_setor());
        }

        return true;
    }

    private void atualizarUI() {
        Log.d("CalendarFragment", "Total de paradas carregadas: " + todasParadas.size());

        // Atualizar adapter
        paradaCalendarAdapter.updateData(todasParadas);

        // Mostrar resumo
        if (!todasParadas.isEmpty()) {
            int emAndamento = 0;
            int finalizadas = 0;

            for (Parada parada : todasParadas) {
                if ("EM_ANDAMENTO".equals(parada.getTipo())) {
                    emAndamento++;
                } else if ("FINALIZADA".equals(parada.getTipo())) {
                    finalizadas++;
                }
            }

            String resumo = String.format("✓ %d paradas carregadas (%d em andamento, %d finalizadas)",
                    todasParadas.size(), emAndamento, finalizadas);
            mostrarMensagemSucesso(resumo);
        } else {
            mostrarMensagemInfo("Nenhuma parada encontrada para seus filtros");
        }
    }

    private void filtrarParadasPorData(CalendarDay dataSelecionada) {
        List<Parada> filtradas = new ArrayList<>();

        for (Parada parada : todasParadas) {
            if (parada.getDt_parada() != null) {
                try {
                    Date date = parada.getDt_parada();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    if (calendar.get(Calendar.YEAR) == dataSelecionada.getYear() &&
                            calendar.get(Calendar.MONTH) == dataSelecionada.getMonth() &&
                            calendar.get(Calendar.DAY_OF_MONTH) == dataSelecionada.getDay()) {
                        filtradas.add(parada);
                    }
                } catch (Exception e) {
                    Log.e("CalendarFragment", "Erro ao filtrar parada por data", e);
                }
            }
        }

        paradaCalendarAdapter.updateData(filtradas);

        if (!filtradas.isEmpty()) {
            mostrarTooltipTiposParadas(filtradas, dataSelecionada);
        } else {
            mostrarMensagemInfo("Nenhuma parada para " + formatarData(dataSelecionada));
        }
    }

    private String formatarData(CalendarDay data) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(data.getYear(), data.getMonth(), data.getDay());
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
        return format.format(calendar.getTime());
    }

    private void mostrarTooltipTiposParadas(List<Parada> paradas, CalendarDay data) {
        int emAndamento = 0;
        int finalizadas = 0;

        for (Parada parada : paradas) {
            if ("EM_ANDAMENTO".equals(parada.getTipo())) {
                emAndamento++;
            } else if ("FINALIZADA".equals(parada.getTipo())) {
                finalizadas++;
            }
        }

        String mensagem = String.format("%s\n● %d em andamento\n● %d finalizadas",
                formatarData(data), emAndamento, finalizadas);

        CustomToast.showInfo(getContext(), mensagem);
    }

    private void mostrarMensagemSucesso(String mensagem) {
        CustomToast.showSuccess(getContext(), mensagem);
    }

    private void mostrarMensagemErro(String mensagem) {
        CustomToast.showError(getContext(), mensagem);
    }

    private void mostrarMensagemInfo(String mensagem) {
        CustomToast.showInfo(getContext(), mensagem);
    }

    private class DiaAtualDecorador implements DayViewDecorator {
        private final CalendarDay hoje;

        public DiaAtualDecorador() {
            this.hoje = CalendarDay.today();
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return hoje.equals(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(Color.TRANSPARENT);
            drawable.setStroke(4, COR_SELECAO);
            view.setBackgroundDrawable(drawable);
            view.addSpan(new ForegroundColorSpan(COR_TEXTO_PRIMARIO));
            view.addSpan(new StyleSpan(Typeface.BOLD));
        }
    }

    private class DiasOutrosMesesDecorador implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            Calendar currentCalendar = Calendar.getInstance();
            Calendar dayCalendar = day.getCalendar();
            return dayCalendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH) ||
                    dayCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(COR_TEXTO_SECUNDARIO));
            view.addSpan(new RelativeSizeSpan(0.8f));
        }
    }

    private class EventoDecorador implements DayViewDecorator {
        private final List<CalendarDay> dates;
        private final Drawable drawable;
        private final String tipo;

        public EventoDecorador(List<CalendarDay> dates, Drawable drawable, String tipo) {
            this.dates = dates;
            this.drawable = drawable;
            this.tipo = tipo;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(drawable);
            view.addSpan(new ForegroundColorSpan(Color.WHITE));
            view.addSpan(new StyleSpan(Typeface.BOLD));
        }
    }

    private class SelecaoDecorador implements DayViewDecorator {
        private final CalendarDay selectedDay;

        public SelecaoDecorador(CalendarDay selectedDay) {
            this.selectedDay = selectedDay;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return selectedDay.equals(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            // Apenas adiciona a borda de seleção, não sobrepõe o fundo
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(Color.TRANSPARENT);
            drawable.setStroke(3, COR_SELECAO);
            view.setBackgroundDrawable(drawable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}