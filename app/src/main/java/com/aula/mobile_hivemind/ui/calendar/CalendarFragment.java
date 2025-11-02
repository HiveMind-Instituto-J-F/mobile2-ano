package com.aula.mobile_hivemind.ui.calendar;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aula.mobile_hivemind.R;
import com.aula.mobile_hivemind.api.RetrofitClient;
import com.aula.mobile_hivemind.api.SqlApiService;
import com.aula.mobile_hivemind.databinding.FragmentCalendarBinding;
import com.aula.mobile_hivemind.dto.ParadaSQLResponseDTO;
import com.aula.mobile_hivemind.dto.RegistroParadaResponseDTO;
import com.aula.mobile_hivemind.recyclerViewParadas.Parada;
import com.aula.mobile_hivemind.recyclerViewParadas.ParadaCalendarAdapter;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    // Cores para diferenciar os tipos de parada
    private static final int COR_EM_ANDAMENTO = Color.rgb(255, 165, 0); // LARANJA
    private static final int COR_FINALIZADA = Color.rgb(0, 128, 0);    // VERDE
    private static final int COR_MISTA = Color.rgb(128, 0, 128);       // ROXO (ambos os tipos)

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar APIs
        sqlApiService = RetrofitClient.getSqlApiService();
        mongoApiService = RetrofitClient.getApiService();
        dateFormat = new SimpleDateFormat("dd, MMM yyyy", Locale.getDefault());

        MaterialCalendarView calendarView = binding.calendarView;

        // Configurar calendário
        setupCalendar(calendarView);

        // Inicializar lista
        todasParadas = new ArrayList<>();

        // Configuração do RecyclerView
        paradaCalendarAdapter = new ParadaCalendarAdapter(getContext(), todasParadas, sqlApiService);
        binding.recyclerViewCalendar.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewCalendar.setAdapter(paradaCalendarAdapter);

        // Carregar paradas de TODAS as fontes
        carregarTodasParadas(calendarView);

        // Listener de clique no calendário
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                if (selected) {
                    filtrarParadasPorData(date);
                }
            }
        });

        return root;
    }

    private void setupCalendar(MaterialCalendarView calendarView) {
        // Título do mês em português
        calendarView.setTitleFormatter(day -> {
            SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", new Locale("pt", "BR"));
            return format.format(day.getDate());
        });

        // Remove setas
        calendarView.setLeftArrowMask(null);
        calendarView.setRightArrowMask(null);

        // Decorador para dias de outros meses
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                Calendar current = Calendar.getInstance();
                return day.getMonth() != current.get(Calendar.MONTH) ||
                        day.getYear() != current.get(Calendar.YEAR);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.addSpan(new ForegroundColorSpan(Color.LTGRAY));
            }
        });

        // Decorador para mês atual
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                Calendar current = Calendar.getInstance();
                return day.getMonth() == current.get(Calendar.MONTH) &&
                        day.getYear() == current.get(Calendar.YEAR);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.addSpan(new ForegroundColorSpan(Color.BLACK));
            }
        });
    }

    private void carregarTodasParadas(MaterialCalendarView calendarView) {
        Log.d("CalendarFragment", "=== INICIANDO CARREGAMENTO DE PARADAS ===");
        Log.d("CalendarFragment", "Tipo de usuário: " + userType);

        // Limpar lista atual
        todasParadas.clear();

        // Carregar paradas do MongoDB (paradas em andamento) - LARANJA
        carregarParadasMongoDB(calendarView);

        // Carregar paradas do SQL (paradas finalizadas) - VERDE
        carregarParadasSQL(calendarView);
    }

    private void carregarParadasMongoDB(MaterialCalendarView calendarView) {
        Log.d("CalendarFragment", "Buscando paradas EM ANDAMENTO do MongoDB...");

        Call<List<RegistroParadaResponseDTO>> call = mongoApiService.getAllRegistros();
        call.enqueue(new Callback<List<RegistroParadaResponseDTO>>() {
            @Override
            public void onResponse(Call<List<RegistroParadaResponseDTO>> call, Response<List<RegistroParadaResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RegistroParadaResponseDTO> paradasMongo = response.body();
                    Log.d("CalendarFragment", "Paradas EM ANDAMENTO encontradas: " + paradasMongo.size());

                    List<Parada> paradasEmAndamento = new ArrayList<>();

                    for (RegistroParadaResponseDTO registro : paradasMongo) {
                        Parada parada = converterParaParada(registro);
                        parada.setTipo("EM_ANDAMENTO"); // Marcar como em andamento

                        if (deveMostrarParada(parada)) {
                            paradasEmAndamento.add(parada);
                            todasParadas.add(parada);
                            Log.d("CalendarFragment", "Parada EM ANDAMENTO - Data: " +
                                    parada.getDt_parada() + ", Setor: " + parada.getDes_setor());
                        }
                    }

                    // Marcar datas no calendário com cor LARANJA
                    marcarParadasNoCalendario(calendarView, paradasEmAndamento, COR_EM_ANDAMENTO);
                    atualizarUI();

                } else {
                    Log.e("CalendarFragment", "Erro ao buscar paradas MongoDB: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<RegistroParadaResponseDTO>> call, Throwable t) {
                Log.e("CalendarFragment", "Falha ao buscar paradas MongoDB: " + t.getMessage());
            }
        });
    }

    private void carregarParadasSQL(MaterialCalendarView calendarView) {
        Log.d("CalendarFragment", "Buscando paradas FINALIZADAS do SQL...");

        Call<List<ParadaSQLResponseDTO>> call = sqlApiService.listarTodasParadas();
        call.enqueue(new Callback<List<ParadaSQLResponseDTO>>() {
            @Override
            public void onResponse(Call<List<ParadaSQLResponseDTO>> call, Response<List<ParadaSQLResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ParadaSQLResponseDTO> paradasSQL = response.body();
                    Log.d("CalendarFragment", "Paradas FINALIZADAS encontradas: " + paradasSQL.size());

                    List<Parada> paradasFinalizadas = new ArrayList<>();

                    for (ParadaSQLResponseDTO registro : paradasSQL) {
                        Parada parada = converterParaParada(registro);
                        parada.setTipo("FINALIZADA");

                        if (deveMostrarParada(parada)) {
                            paradasFinalizadas.add(parada);
                            todasParadas.add(parada);
                            Log.d("CalendarFragment", "Parada FINALIZADA - Data: " +
                                    parada.getDt_parada() + ", Setor: " + parada.getDes_setor());
                        }
                    }

                    marcarParadasNoCalendario(calendarView, paradasFinalizadas, COR_FINALIZADA);
                    atualizarUI();

                } else {
                    Log.e("CalendarFragment", "Erro ao buscar paradas SQL: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("CalendarFragment", "Corpo do erro: " + errorBody);
                        } catch (Exception e) {
                            Log.e("CalendarFragment", "Erro ao ler corpo do erro", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ParadaSQLResponseDTO>> call, Throwable t) {
                Log.e("CalendarFragment", "Falha ao buscar paradas SQL: " + t.getMessage());
            }
        });
    }

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
            SimpleDateFormat[] formatos = {
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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

        // Mostrar mensagem se não houver paradas
        if (todasParadas.isEmpty()) {
            Toast.makeText(getContext(), "Nenhuma parada encontrada", Toast.LENGTH_SHORT).show();
        }
    }

    private void marcarParadasNoCalendario(MaterialCalendarView calendarView, List<Parada> paradas, int cor) {
        Map<CalendarDay, Integer> mapaCores = new HashMap<>();

        for (Parada parada : paradas) {
            if (parada.getDt_parada() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(parada.getDt_parada());
                CalendarDay dia = CalendarDay.from(calendar);

                Integer corExistente = mapaCores.get(dia);
                if (corExistente == null) {
                    mapaCores.put(dia, cor);
                } else if (corExistente != cor) {
                    mapaCores.put(dia, COR_MISTA);
                }
            }
        }

        // Aplicar decoradores
        for (Map.Entry<CalendarDay, Integer> entry : mapaCores.entrySet()) {
            calendarView.addDecorator(new EventoDecorador(
                    entry.getKey(),
                    new ColorDrawable(entry.getValue())
            ));
        }

        calendarView.invalidateDecorators();
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
        Log.d("CalendarFragment", "Paradas filtradas para " + dataSelecionada + ": " + filtradas.size());

        // Mostrar tooltip com tipos de paradas
        if (!filtradas.isEmpty()) {
            mostrarTooltipTiposParadas(filtradas);
        }
    }

    private void mostrarTooltipTiposParadas(List<Parada> paradas) {
        int emAndamento = 0;
        int finalizadas = 0;

        for (Parada parada : paradas) {
            if ("EM_ANDAMENTO".equals(parada.getTipo())) {
                emAndamento++;
            } else if ("FINALIZADA".equals(parada.getTipo())) {
                finalizadas++;
            }
        }

        String mensagem = "Paradas: ";
        if (emAndamento > 0) {
            mensagem += emAndamento + " em andamento ";
        }
        if (finalizadas > 0) {
            mensagem += finalizadas + " finalizadas";
        }

        Toast.makeText(getContext(), mensagem, Toast.LENGTH_SHORT).show();
    }

    // Decorador para eventos com tipo
    private static class EventoDecorador implements DayViewDecorator {
        private final CalendarDay date;
        private final Drawable backgroundDrawable;

        public EventoDecorador(CalendarDay date, Drawable backgroundDrawable) {
            this.date = date;
            this.backgroundDrawable = backgroundDrawable;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return date.equals(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(backgroundDrawable);
            view.addSpan(new ForegroundColorSpan(Color.WHITE));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}